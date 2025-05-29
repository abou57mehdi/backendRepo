package com.ESI.CareerBooster.cv.controller;

import com.ESI.CareerBooster.cv.model.CV;
import com.ESI.CareerBooster.cv.model.CVScore;
import com.ESI.CareerBooster.cv.repository.CVScoreRepository;
import com.ESI.CareerBooster.auth.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CVScoreControllerTest {

    @Mock
    private CVScoreRepository cvScoreRepository;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private CVScoreController cvScoreController;

    private CVScore sampleScore;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        // Initialize sample user
        sampleUser = new User();
        sampleUser.setEmail("test@example.com");

        // Initialize sample CV
        CV sampleCV = new CV();
        sampleCV.setId(1L);
        sampleCV.setUser(sampleUser);

        // Initialize sample score
        sampleScore = new CVScore();
        sampleScore.setId(1L);
        sampleScore.setCv(sampleCV);
        sampleScore.setOverallScore(85);
        sampleScore.setIndustryType("TECHNOLOGY");
        sampleScore.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getUserCVScores_ValidUser_ReturnsScores() {
        // Arrange
        List<CVScore> scores = Arrays.asList(sampleScore);
        when(cvScoreRepository.findByUserEmailOrderByCreatedAtDesc(anyString())).thenReturn(scores);

        // Act
        ResponseEntity<List<CVScore>> response = cvScoreController.getUserCVScores(userDetails);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(sampleScore.getId(), response.getBody().get(0).getId());
        verify(cvScoreRepository).findByUserEmailOrderByCreatedAtDesc(userDetails.getUsername());
    }

    @Test
    void getLatestCVScore_ValidUser_ReturnsScore() {
        // Arrange
        when(cvScoreRepository.getLatestScoreByUserEmail(anyString())).thenReturn(Optional.of(sampleScore));

        // Act
        ResponseEntity<CVScore> response = cvScoreController.getLatestCVScore(userDetails);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(sampleScore.getId(), response.getBody().getId());
        verify(cvScoreRepository).getLatestScoreByUserEmail(userDetails.getUsername());
    }

    @Test
    void getLatestCVScore_NoScores_ReturnsNotFound() {
        // Arrange
        when(cvScoreRepository.getLatestScoreByUserEmail(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<CVScore> response = cvScoreController.getLatestCVScore(userDetails);

        // Assert
        assertNotNull(response);
        assertEquals(404, response.getStatusCodeValue());
        verify(cvScoreRepository).getLatestScoreByUserEmail(userDetails.getUsername());
    }

    @Test
    void getAverageCVScore_ValidUser_ReturnsAverage() {
        // Arrange
        when(cvScoreRepository.getAverageScoreByUserEmail(anyString())).thenReturn(85.0);

        // Act
        ResponseEntity<Double> response = cvScoreController.getAverageCVScore(userDetails);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(85.0, response.getBody());
        verify(cvScoreRepository).getAverageScoreByUserEmail(userDetails.getUsername());
    }

    @Test
    void getAverageCVScore_NoScores_ReturnsZero() {
        // Arrange
        when(cvScoreRepository.getAverageScoreByUserEmail(anyString())).thenReturn(null);

        // Act
        ResponseEntity<Double> response = cvScoreController.getAverageCVScore(userDetails);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(0.0, response.getBody());
        verify(cvScoreRepository).getAverageScoreByUserEmail(userDetails.getUsername());
    }

    @Test
    void getCVScore_ValidIdAndUser_ReturnsScore() {
        // Arrange
        when(cvScoreRepository.findByCvId(any())).thenReturn(Optional.of(sampleScore));

        // Act
        ResponseEntity<CVScore> response = cvScoreController.getCVScore(1L, userDetails);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(sampleScore.getId(), response.getBody().getId());
        verify(cvScoreRepository).findByCvId(1L);
    }

    @Test
    void getCVScore_InvalidId_ReturnsNotFound() {
        // Arrange
        when(cvScoreRepository.findByCvId(any())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<CVScore> response = cvScoreController.getCVScore(999L, userDetails);

        // Assert
        assertNotNull(response);
        assertEquals(404, response.getStatusCodeValue());
        verify(cvScoreRepository).findByCvId(999L);
    }

    @Test
    void getCVScore_DifferentUser_ReturnsForbidden() {
        // Arrange
        User differentUser = new User();
        differentUser.setEmail("different@example.com");
        sampleScore.getCv().setUser(differentUser);
        when(cvScoreRepository.findByCvId(any())).thenReturn(Optional.of(sampleScore));

        // Act
        ResponseEntity<CVScore> response = cvScoreController.getCVScore(1L, userDetails);

        // Assert
        assertNotNull(response);
        assertEquals(403, response.getStatusCodeValue());
        verify(cvScoreRepository).findByCvId(1L);
    }

    @Test
    void getCVScoresByIndustry_ValidIndustry_ReturnsScores() {
        // Arrange
        List<CVScore> scores = Arrays.asList(sampleScore);
        when(cvScoreRepository.findByUserEmailOrderByCreatedAtDesc(anyString())).thenReturn(scores);

        // Act
        ResponseEntity<List<CVScore>> response = cvScoreController.getCVScoresByIndustry("TECHNOLOGY", userDetails);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(sampleScore.getId(), response.getBody().get(0).getId());
        verify(cvScoreRepository).findByUserEmailOrderByCreatedAtDesc(userDetails.getUsername());
    }

    @Test
    void getCVScoresByIndustry_NoMatchingScores_ReturnsEmptyList() {
        // Arrange
        List<CVScore> scores = Arrays.asList(sampleScore);
        when(cvScoreRepository.findByUserEmailOrderByCreatedAtDesc(anyString())).thenReturn(scores);

        // Act
        ResponseEntity<List<CVScore>> response = cvScoreController.getCVScoresByIndustry("FINANCE", userDetails);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(cvScoreRepository).findByUserEmailOrderByCreatedAtDesc(userDetails.getUsername());
    }
} 