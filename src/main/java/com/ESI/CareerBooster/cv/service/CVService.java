package com.ESI.CareerBooster.cv.service;

import com.ESI.CareerBooster.cv.dto.CVUploadResponse;
import com.ESI.CareerBooster.cv.dto.CourseRecommendation;
import com.ESI.CareerBooster.cv.repository.CVRepository;
import com.ESI.CareerBooster.cv.model.CV;
import com.ESI.CareerBooster.auth.model.User;
import com.ESI.CareerBooster.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ESI.CareerBooster.cv.dto.CVAnalysisResponse;
import org.apache.tika.Tika;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.io.InputStream;
import com.ESI.CareerBooster.cv.service.AIService;
import com.ESI.CareerBooster.cv.model.CVScore;

@Slf4j
@Service
@RequiredArgsConstructor
public class CVService {
    private final CVRepository cvRepository;
    private final UserRepository userRepository;
    private final AIService aiService;
    private final CVAnalyzerService cvAnalyzerService;
    private final ObjectMapper objectMapper;
    private final Tika tika;

    public CVUploadResponse processCV(MultipartFile file, String userEmail, String analysisType) throws IOException {
        log.debug("Processing CV for user: {} with analysis type: {}", userEmail, analysisType);

        // Extract text from PDF
        String cvContent = extractTextFromPDF(file);
        log.debug("Extracted CV content length: {}", cvContent.length());

        CVAnalysisResponse analysisResult = null;
        String recommendationsText = null;
        List<CVAnalysisResponse.CourseRecommendation> courseRecommendationsList = null;

        // Determine which AI analysis to perform based on analysisType
        if ("general_analysis".equals(analysisType)) {
            log.debug("Requesting general CV analysis from AI");
            // Call AI for general analysis (strengths, weaknesses, etc.)
            recommendationsText = aiService.analyzeCVGeneral(cvContent); // New method for general analysis
        } else { // Default to course recommendations if type is not specified or unknown
            log.debug("Requesting course recommendations from AI");
            // Call AI for course recommendations (existing logic)
            analysisResult = aiService.analyzeCV(cvContent);
            recommendationsText = analysisResult.getTextAnalysis(); // Get text analysis (should be the course list)
            courseRecommendationsList = analysisResult.getRecommendations(); // Get structured course list if available
        }


        // Save CV to database
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));

        CV cv = new CV();
        cv.setUser(user);
        cv.setFileName(file.getOriginalFilename());
        cv.setContent(cvContent);
        cv.setRecommendations(recommendationsText); // Save the appropriate text analysis
        CV savedCV = cvRepository.save(cv);

        // Analyze CV and calculate score
        log.debug("Starting CV scoring analysis for: {}", file.getOriginalFilename());
        CVScore cvScore = cvAnalyzerService.analyzeCV(savedCV);
        log.debug("CV scoring completed. Overall score: {}", cvScore.getOverallScore());

        // Return the appropriate response
        return new CVUploadResponse(
            file.getOriginalFilename(),
            recommendationsText, // Return the text analysis/recommendations
            courseRecommendationsList // Return course list (will be null for general analysis)
        );
    }

    public String extractTextFromPDF(MultipartFile file) throws IOException {
        log.debug("Starting PDF text extraction for file: {}", file.getOriginalFilename());
        Path tmp = null;
        try {
            // Validate file type
            String mimeType = tika.detect(file.getInputStream());
            if (!mimeType.equals("application/pdf")) {
                throw new IOException("Invalid file type. Expected PDF but got: " + mimeType);
            }

            // 1. Persist to tmp so PDFBox can re-open as RandomAccess
            tmp = Files.createTempFile("cv_", ".pdf");
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
            }
            log.debug("File copied to temporary location: {}", tmp);

            // Use Loader.loadPDF with File, password, scratchFile (null), and lenient flag
            try (PDDocument doc = Loader.loadPDF(tmp.toFile())) { // Simplified to use default parameters
                if (doc.isEncrypted()) {
                    log.warn("Password-protected PDF detected: {}", file.getOriginalFilename());
                    throw new IOException("PDF is password-protected");
                }
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setSortByPosition(true);
                String text = stripper.getText(doc);

                if (text == null || text.trim().isEmpty()) {
                    log.warn("No extractable text from PDF: {}", file.getOriginalFilename());
                    throw new IOException("No extractable text found in PDF. It might be a scanned image or corrupted.");
                }

                log.debug("Successfully extracted text (length {}): {}", text.length(), text.substring(0, Math.min(text.length(), 200))); // Log a preview
                return text;
            } catch (InvalidPasswordException e) {
                 log.error("PDF password error for file {}: {}", file.getOriginalFilename(), e.getMessage());
                 throw new IOException("PDF is password-protected", e);
            } catch (java.lang.SecurityException e) {
                 log.error("PDF security error for file {}: {}", file.getOriginalFilename(), e.getMessage());
                 throw new IOException("PDF has security restrictions", e);
            } catch (IOException e) {
                log.error("PDFBox extraction failed for file {}: {}", file.getOriginalFilename(), e.getMessage(), e);
                throw new IOException("Failed to extract text from PDF: " + e.getMessage(), e);
            }
        } catch (IOException e) {
            log.error("File operation error for file {}: {}", file.getOriginalFilename(), e.getMessage(), e);
            throw new IOException("File processing error: " + e.getMessage(), e);
        } finally {
            if (tmp != null) {
                try {
                    Files.deleteIfExists(tmp);
                    log.debug("Temporary file deleted: {}", tmp);
                } catch (IOException e) {
                    log.error("Failed to delete temporary file {}: {}", tmp, e.getMessage(), e);
                }
            }
        }
    }
}