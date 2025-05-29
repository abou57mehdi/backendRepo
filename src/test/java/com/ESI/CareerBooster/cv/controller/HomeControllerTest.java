package com.ESI.CareerBooster.cv.controller;

import com.ESI.CareerBooster.cv.dto.HomeScreenData;
import com.ESI.CareerBooster.cv.service.HomeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class HomeControllerTest {

    @Mock
    private HomeService homeService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private HomeController homeController;

    private HomeScreenData sampleHomeData;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        // Initialize sample home data
        sampleHomeData = new HomeScreenData();

        // Set user progress
        HomeScreenData.UserProgress progress = new HomeScreenData.UserProgress();
        progress.setCompletedAnalyses(5);
        progress.setCompletedCourses(3);
        progress.setSkillScore(85);
        progress.setLastAnalysisDate(LocalDateTime.now());
        progress.setIndustryType("TECHNOLOGY");
        progress.setAverageScore(82.5);
        progress.setLatestScore(85);
        sampleHomeData.setUserProgress(progress);

        // Set recent analyses
        List<HomeScreenData.RecentAnalysis> recentAnalyses = new ArrayList<>();
        HomeScreenData.RecentAnalysis analysis = new HomeScreenData.RecentAnalysis();
        analysis.setId("1");
        analysis.setFileName("test_cv.pdf");
        analysis.setAnalysisDate(LocalDateTime.now());
        analysis.setSummary("Recent CV Analysis");
        
        List<HomeScreenData.AnalysisPoint> strengths = new ArrayList<>();
        HomeScreenData.AnalysisPoint strength = new HomeScreenData.AnalysisPoint();
        strength.setTitle("Strong Technical Skills");
        strength.setDescription("Excellent programming skills");
        strength.setIcon("💪");
        strengths.add(strength);
        analysis.setStrengths(strengths);

        List<HomeScreenData.AnalysisPoint> improvements = new ArrayList<>();
        HomeScreenData.AnalysisPoint improvement = new HomeScreenData.AnalysisPoint();
        improvement.setTitle("Project Experience");
        improvement.setDescription("Add more project details");
        improvement.setIcon("🎯");
        improvements.add(improvement);
        analysis.setImprovements(improvements);

        recentAnalyses.add(analysis);
        sampleHomeData.setRecentAnalyses(recentAnalyses);

        // Set CV status
        HomeScreenData.CVStatus cvStatus = new HomeScreenData.CVStatus();
        cvStatus.setStatus("COMPLETED");
        cvStatus.setMessage("Analysis complete");
        cvStatus.setLastUpdate(LocalDateTime.now());
        sampleHomeData.setCvStatus(cvStatus);
    }

    @Test
    void getHomeData_ValidUser_ReturnsSuccess() {
        // Arrange
        when(homeService.getHomeData(anyString())).thenReturn(sampleHomeData);

        // Act
        ResponseEntity<HomeScreenData> response = homeController.getHomeData(userDetails);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        
        // Verify user progress
        HomeScreenData.UserProgress progress = response.getBody().getUserProgress();
        assertNotNull(progress);
        assertEquals(5, progress.getCompletedAnalyses());
        assertEquals(3, progress.getCompletedCourses());
        assertEquals(85, progress.getSkillScore());
        assertEquals("TECHNOLOGY", progress.getIndustryType());
        assertEquals(82.5, progress.getAverageScore());
        assertEquals(85, progress.getLatestScore());

        // Verify recent analyses
        List<HomeScreenData.RecentAnalysis> analyses = response.getBody().getRecentAnalyses();
        assertNotNull(analyses);
        assertEquals(1, analyses.size());
        assertEquals("test_cv.pdf", analyses.get(0).getFileName());
        assertEquals(1, analyses.get(0).getStrengths().size());
        assertEquals(1, analyses.get(0).getImprovements().size());

        // Verify CV status
        HomeScreenData.CVStatus status = response.getBody().getCvStatus();
        assertNotNull(status);
        assertEquals("COMPLETED", status.getStatus());
        assertEquals("Analysis complete", status.getMessage());

        verify(homeService).getHomeData(userDetails.getUsername());
    }

    @Test
    void getHomeData_ServiceError_ReturnsInternalServerError() {
        // Arrange
        when(homeService.getHomeData(anyString())).thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<HomeScreenData> response = homeController.getHomeData(userDetails);

        // Assert
        assertNotNull(response);
        assertEquals(500, response.getStatusCodeValue());
        verify(homeService).getHomeData(userDetails.getUsername());
    }
} 