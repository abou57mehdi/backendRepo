package com.ESI.CareerBooster.cv.controller;

import com.ESI.CareerBooster.cv.dto.CVUploadResponse;
import com.ESI.CareerBooster.cv.dto.EnhancedCVAnalysisResponse;
import com.ESI.CareerBooster.cv.dto.ScoreHistoryDTO;
import com.ESI.CareerBooster.cv.service.CVService;
import com.ESI.CareerBooster.cv.service.EnhancedCVScoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import com.ESI.CareerBooster.auth.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@RestController
@RequestMapping("/api/cv")
@RequiredArgsConstructor
@Tag(name = "CV Management", description = "APIs for CV upload and processing")
@CrossOrigin(origins = {
    "http://localhost:8081",
    "http://192.168.100.155:8081",
    "exp://192.168.100.155:8081"
}, allowCredentials = "true")
public class CVController {
    private final CVService cvService;
    private final JwtUtil jwtUtil;
    private final EnhancedCVScoringService enhancedCVScoringService;

    @Operation(
        summary = "Upload and process CV",
        description = "Upload a PDF CV file for processing. Requires authentication.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "CV processed successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CVUploadResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid file or no file uploaded"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadCV(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request,
            @RequestParam(value = "analysisType", required = false) String analysisType) {
        log.info("=== CV Upload Request Received ===");
        log.info("Request URL: {}", request.getRequestURL());
        log.info("Content Type: {}", request.getContentType());
        log.info("Method: {}", request.getMethod());
        log.info("Analysis Type: {}", analysisType);

        // Log file details
        log.info("File Details:");
        log.info("- Name: {}", file.getOriginalFilename());
        log.info("- Size: {} bytes", file.getSize());
        log.info("- Content Type: {}", file.getContentType());

        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                log.error("No file uploaded or file is empty");
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "error", "No file uploaded",
                        "message", "Please select a PDF file to upload"
                    ));
            }

            if (!file.getContentType().equals("application/pdf")) {
                log.error("Invalid file type: {}", file.getContentType());
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "error", "Invalid file type",
                        "message", "Only PDF files are allowed",
                        "receivedType", file.getContentType()
                    ));
            }

            // Get user email from SecurityContext
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            log.info("Processing CV for user: {}", userEmail);

            // Process the file, passing analysisType
            log.info("Starting CV processing...");
            CVUploadResponse response = cvService.processCV(file, userEmail, analysisType);
            log.info("CV processing completed successfully");
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Error processing CV: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Error processing CV",
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Unexpected error",
                    "message", e.getMessage(),
                    "type", e.getClass().getName()
                ));
        }
    }

    @Operation(
        summary = "Enhanced CV Analysis",
        description = "Analyze CV with enhanced scoring algorithm and detailed breakdown",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Enhanced analysis completed successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = EnhancedCVAnalysisResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid file or parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/analyze-enhanced", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyzeEnhanced(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "industry", required = false) String industry,
            HttpServletRequest request) {
        try {
            log.info("Enhanced CV analysis request received");

            // Validate file
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "No file uploaded"));
            }

            if (!file.getContentType().equals("application/pdf")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Only PDF files are allowed"));
            }

            // Get user email from SecurityContext
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            log.info("Processing enhanced analysis for user: {}", userEmail);

            // Analyze with enhanced service
            EnhancedCVAnalysisResponse response = enhancedCVScoringService.analyzeCV(file, userEmail, industry);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error in enhanced CV analysis: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error processing enhanced analysis", "message", e.getMessage()));
        }
    }

    @Operation(
        summary = "Get Score History",
        description = "Get detailed score history and analytics for the authenticated user",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Score history retrieved successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ScoreHistoryDTO.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/score-history")
    public ResponseEntity<?> getScoreHistory(
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {
        try {
            // Get user email from SecurityContext
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            log.info("Getting score history for user: {} with limit: {}", userEmail, limit);

            ScoreHistoryDTO response = enhancedCVScoringService.getScoreHistory(userEmail, limit);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting score history: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error retrieving score history", "message", e.getMessage()));
        }
    }

    @Operation(
        summary = "Get Industry Benchmark",
        description = "Get industry benchmark data for comparison",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/industry-benchmark/{industry}")
    public ResponseEntity<?> getIndustryBenchmark(@PathVariable String industry) {
        try {
            log.info("Getting industry benchmark for: {}", industry);

            // Simple benchmark data - could be enhanced with real data
            Map<String, Object> benchmark = Map.of(
                "industry", industry.toUpperCase(),
                "averageScore", getIndustryAverageScore(industry),
                "topPercentileScore", getIndustryTopScore(industry),
                "sampleSize", 1000 // Mock data
            );

            return ResponseEntity.ok(benchmark);

        } catch (Exception e) {
            log.error("Error getting industry benchmark: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error retrieving benchmark data", "message", e.getMessage()));
        }
    }

    // Helper methods for benchmark data
    private int getIndustryAverageScore(String industry) {
        return switch (industry.toUpperCase()) {
            case "TECHNOLOGY" -> 78;
            case "MARKETING" -> 75;
            case "FINANCE" -> 80;
            case "HEALTHCARE" -> 76;
            case "EDUCATION" -> 74;
            default -> 75;
        };
    }

    private int getIndustryTopScore(String industry) {
        return switch (industry.toUpperCase()) {
            case "TECHNOLOGY" -> 95;
            case "MARKETING" -> 92;
            case "FINANCE" -> 97;
            case "HEALTHCARE" -> 93;
            case "EDUCATION" -> 91;
            default -> 93;
        };
    }
}