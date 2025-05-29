package com.ESI.CareerBooster.cv.controller;

import com.ESI.CareerBooster.cv.dto.CVUploadResponse;
import com.ESI.CareerBooster.cv.dto.EnhancedCVAnalysisResponse;
import com.ESI.CareerBooster.cv.service.CVService;
import com.ESI.CareerBooster.cv.service.EnhancedCVScoringService;
import com.ESI.CareerBooster.auth.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CVControllerTest {

    @Mock
    private CVService cvService;

    @Mock
    private EnhancedCVScoringService enhancedCVScoringService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CVController cvController;

    private MockHttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");

        // Setup request mock
        mockRequest = new MockHttpServletRequest();
        mockRequest.setScheme("http");
        mockRequest.setServerName("localhost");
        mockRequest.setServerPort(8080);
        mockRequest.setRequestURI("/api/cv/upload");
        mockRequest.setMethod("POST");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
    }

    @Test
    void uploadCV_ValidPdfFile_ReturnsSuccess() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "test pdf content".getBytes()
        );

        CVUploadResponse expectedResponse = new CVUploadResponse(
            "test.pdf",
            "Your CV has been analyzed successfully",
            new ArrayList<>()
        );
        when(cvService.processCV(any(), anyString(), anyString())).thenReturn(expectedResponse);

        // Act
        ResponseEntity<?> response = cvController.uploadCV(file, mockRequest, "basic");

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        verify(cvService).processCV(any(), anyString(), anyString());
    }

    @Test
    void analyzeEnhanced_ValidPdfFile_ReturnsSuccess() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "test pdf content".getBytes()
        );

        EnhancedCVAnalysisResponse expectedResponse = new EnhancedCVAnalysisResponse();
        expectedResponse.setOverallScore(85);
        expectedResponse.setGrade("A");
        expectedResponse.setIndustryType("TECHNOLOGY");
        expectedResponse.setCareerLevel("SENIOR");
        expectedResponse.setIndustryBenchmark(78);
        expectedResponse.setTrend("IMPROVING");
        expectedResponse.setAnalysisDate(LocalDateTime.now());
        expectedResponse.setAtsCompatibilityScore(92);

        // Set section scores
        EnhancedCVAnalysisResponse.SectionScores sectionScores = new EnhancedCVAnalysisResponse.SectionScores();
        sectionScores.setContactInfo(15);
        sectionScores.setSummary(12);
        sectionScores.setExperience(28);
        sectionScores.setEducation(14);
        sectionScores.setSkills(25);
        sectionScores.setProjects(8);
        sectionScores.setFormatting(18);
        sectionScores.setKeywords(12);
        expectedResponse.setSectionScores(sectionScores);

        // Set recommendations
        expectedResponse.setRecommendations(List.of(
            "Add quantifiable achievements to your experience section",
            "Include more industry-specific technical skills",
            "Consider adding a projects section to showcase your work"
        ));

        // Set score comparison
        EnhancedCVAnalysisResponse.ScoreComparison comparison = new EnhancedCVAnalysisResponse.ScoreComparison();
        comparison.setPercentileRank(75);
        comparison.setPointsDifference(7);
        comparison.setStatus("ABOVE_AVERAGE");
        expectedResponse.setScoreComparison(comparison);

        when(enhancedCVScoringService.analyzeCV(any(), anyString(), anyString())).thenReturn(expectedResponse);

        // Act
        ResponseEntity<?> response = cvController.analyzeEnhanced(file, "IT", mockRequest);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        verify(enhancedCVScoringService).analyzeCV(any(), anyString(), anyString());
    }

    @Test
    void uploadCV_InvalidFileType_ReturnsBadRequest() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "test content".getBytes()
        );

        // Act
        ResponseEntity<?> response = cvController.uploadCV(file, mockRequest, "basic");

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        verify(cvService, never()).processCV(any(), anyString(), anyString());
    }
} 