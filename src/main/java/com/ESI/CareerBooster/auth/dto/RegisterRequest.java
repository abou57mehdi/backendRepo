package com.ESI.CareerBooster.auth.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Registration request containing user information")
public class RegisterRequest {
    @Schema(description = "User's full name", example = "John Doe", required = true)
    private String name;

    @Schema(description = "User's email address", example = "john.doe@example.com", required = true)
    private String email;

    @Schema(description = "User's password", example = "password123", required = true)
    private String password;
} 