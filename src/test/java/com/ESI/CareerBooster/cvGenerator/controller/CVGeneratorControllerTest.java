package com.ESI.CareerBooster.cvGenerator.controller;

import com.ESI.CareerBooster.cvGenerator.dto.CVGeneratorDTO;
import com.ESI.CareerBooster.cvGenerator.model.CVGenerator;
import com.ESI.CareerBooster.cvGenerator.service.CVGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CVGeneratorControllerTest {

    @Mock
    private CVGeneratorService cvGeneratorService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CVGeneratorController cvGeneratorController;

    private CVGeneratorDTO sampleCVDTO;
    private CVGenerator sampleCV;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(authentication.getName()).thenReturn("test@example.com");

        // Initialize sample CV data
        sampleCVDTO = new CVGeneratorDTO();
        sampleCVDTO.setId(1L);
        sampleCVDTO.setSummary("Professional summary");
        sampleCVDTO.setSkills(new ArrayList<>(List.of("Java", "Spring", "React")));
        sampleCVDTO.setTemplate("modern");
        sampleCVDTO.setCreatedDate(LocalDateTime.now());
        sampleCVDTO.setLastModifiedDate(LocalDateTime.now());

        sampleCV = new CVGenerator();
        sampleCV.setId(1L);
        sampleCV.setSummary("Professional summary");
        sampleCV.setSkills(new ArrayList<>(List.of("Java", "Spring", "React")));
        sampleCV.setTemplate("modern");
        sampleCV.setCreatedDate(LocalDateTime.now());
        sampleCV.setLastModifiedDate(LocalDateTime.now());
    }

    @Test
    void createCV_ValidRequest_ReturnsCreated() {
        // Arrange
        when(cvGeneratorService.createCV(any(), anyString())).thenReturn(sampleCV);

        // Act
        ResponseEntity<CVGeneratorDTO> response = cvGeneratorController.createCV(sampleCVDTO, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(sampleCVDTO.getId(), response.getBody().getId());
        verify(cvGeneratorService).createCV(any(), anyString());
    }

    @Test
    void getAllCVs_ValidRequest_ReturnsList() {
        // Arrange
        List<CVGenerator> cvs = List.of(sampleCV);
        when(cvGeneratorService.getAllCVsByUserId(anyString())).thenReturn(cvs);

        // Act
        ResponseEntity<List<CVGeneratorDTO>> response = cvGeneratorController.getAllCVs(authentication);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(cvGeneratorService).getAllCVsByUserId(anyString());
    }

    @Test
    void getCVById_ValidId_ReturnsCV() {
        // Arrange
        when(cvGeneratorService.getCVById(any(), anyString())).thenReturn(Optional.of(sampleCV));

        // Act
        ResponseEntity<CVGeneratorDTO> response = cvGeneratorController.getCVById(1L, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(sampleCVDTO.getId(), response.getBody().getId());
        verify(cvGeneratorService).getCVById(any(), anyString());
    }

    @Test
    void getCVById_InvalidId_ReturnsNotFound() {
        // Arrange
        when(cvGeneratorService.getCVById(any(), anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<CVGeneratorDTO> response = cvGeneratorController.getCVById(999L, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(cvGeneratorService).getCVById(any(), anyString());
    }

    @Test
    void updateCV_ValidRequest_ReturnsUpdated() {
        // Arrange
        when(cvGeneratorService.updateCV(any(), any(), anyString())).thenReturn(sampleCV);

        // Act
        ResponseEntity<CVGeneratorDTO> response = cvGeneratorController.updateCV(1L, sampleCVDTO, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(sampleCVDTO.getId(), response.getBody().getId());
        verify(cvGeneratorService).updateCV(any(), any(), anyString());
    }

    @Test
    void updateCV_InvalidId_ReturnsNotFound() {
        // Arrange
        when(cvGeneratorService.updateCV(any(), any(), anyString()))
            .thenThrow(new RuntimeException("CV not found or access denied"));

        // Act
        ResponseEntity<CVGeneratorDTO> response = cvGeneratorController.updateCV(999L, sampleCVDTO, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(cvGeneratorService).updateCV(any(), any(), anyString());
    }

    @Test
    void deleteCV_ValidId_ReturnsNoContent() {
        // Arrange
        when(cvGeneratorService.deleteCV(any(), anyString())).thenReturn(true);

        // Act
        ResponseEntity<Void> response = cvGeneratorController.deleteCV(1L, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(cvGeneratorService).deleteCV(any(), anyString());
    }

    @Test
    void deleteCV_InvalidId_ReturnsNotFound() {
        // Arrange
        when(cvGeneratorService.deleteCV(any(), anyString())).thenReturn(false);

        // Act
        ResponseEntity<Void> response = cvGeneratorController.deleteCV(999L, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(cvGeneratorService).deleteCV(any(), anyString());
    }

    @Test
    void exportCVAsPdf_ValidId_ReturnsPdf() {
        // Arrange
        byte[] pdfContent = "PDF content".getBytes();
        when(cvGeneratorService.generateCVAsPdf(any(), anyString(), anyString())).thenReturn(pdfContent);

        // Act
        ResponseEntity<byte[]> response = cvGeneratorController.exportCVAsPdf(1L, "modern", authentication);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertArrayEquals(pdfContent, response.getBody());
        verify(cvGeneratorService).generateCVAsPdf(any(), anyString(), anyString());
    }

    @Test
    void exportCVAsPdf_InvalidId_ReturnsNotFound() {
        // Arrange
        when(cvGeneratorService.generateCVAsPdf(any(), anyString(), anyString()))
            .thenThrow(new RuntimeException("CV not found or access denied"));

        // Act
        ResponseEntity<byte[]> response = cvGeneratorController.exportCVAsPdf(999L, "modern", authentication);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(cvGeneratorService).generateCVAsPdf(any(), anyString(), anyString());
    }
} 