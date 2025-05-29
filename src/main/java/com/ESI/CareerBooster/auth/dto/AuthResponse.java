package com.ESI.CareerBooster.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Schema(description = "Authentication response containing JWT token and user information")
public class AuthResponse {
    @Schema(description = "JWT authentication token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "User's full name", example = "John Doe")
    private String name;

    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "User registration date", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
}