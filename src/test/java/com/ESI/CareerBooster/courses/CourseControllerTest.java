package com.ESI.CareerBooster.courses;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CourseControllerTest {

    @Mock
    private CourseraService courseraService;

    @InjectMocks
    private CourseController courseController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getCourses_ValidCategory_ReturnsSuccess() {
        // Arrange
        String category = "programming";
        String expectedResponse = "{\"elements\":[{\"name\":\"Java Programming\",\"description\":\"Learn Java\"}]}";
        when(courseraService.fetchCoursesByCategory(anyString())).thenReturn(expectedResponse);

        // Act
        ResponseEntity<String> response = courseController.getCourses(category);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(expectedResponse, response.getBody());
        verify(courseraService).fetchCoursesByCategory(category);
    }

    @Test
    void getCourses_EmptyCategory_ReturnsSuccess() {
        // Arrange
        String category = "";
        String expectedResponse = "{\"elements\":[]}";
        when(courseraService.fetchCoursesByCategory(anyString())).thenReturn(expectedResponse);

        // Act
        ResponseEntity<String> response = courseController.getCourses(category);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(expectedResponse, response.getBody());
        verify(courseraService).fetchCoursesByCategory(category);
    }

    @Test
    void getCourses_ServiceError_ReturnsErrorResponse() {
        // Arrange
        String category = "invalid";
        String errorResponse = "{\"elements\":[],\"error\":\"Invalid category\"}";
        when(courseraService.fetchCoursesByCategory(anyString())).thenReturn(errorResponse);

        // Act
        ResponseEntity<String> response = courseController.getCourses(category);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(errorResponse, response.getBody());
        verify(courseraService).fetchCoursesByCategory(category);
    }
} 