package com.ESI.CareerBooster.auth.controller;

import com.ESI.CareerBooster.auth.dto.AuthResponse;
import com.ESI.CareerBooster.auth.dto.LoginRequest;
import com.ESI.CareerBooster.auth.dto.RegisterRequest;
import com.ESI.CareerBooster.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_ValidRequest_ReturnsSuccess() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");

        AuthResponse expectedResponse = new AuthResponse(
            "jwt-token",
            "John Doe",
            "john@example.com",
            LocalDateTime.now()
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(expectedResponse);

        // Act
        ResponseEntity<AuthResponse> response = authController.register(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("jwt-token", response.getBody().getToken());
        assertEquals("John Doe", response.getBody().getName());
        assertEquals("john@example.com", response.getBody().getEmail());
    }

    @Test
    void login_ValidCredentials_ReturnsSuccess() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("password123");

        AuthResponse expectedResponse = new AuthResponse(
            "jwt-token",
            "John Doe",
            "john@example.com",
            LocalDateTime.now()
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(expectedResponse);

        // Act
        ResponseEntity<AuthResponse> response = authController.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("jwt-token", response.getBody().getToken());
        assertEquals("John Doe", response.getBody().getName());
        assertEquals("john@example.com", response.getBody().getEmail());
    }
} 