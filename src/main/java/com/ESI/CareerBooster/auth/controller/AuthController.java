package com.ESI.CareerBooster.auth.controller;

import com.ESI.CareerBooster.auth.dto.AuthResponse;
import com.ESI.CareerBooster.auth.dto.LoginRequest;
import com.ESI.CareerBooster.auth.dto.RegisterRequest;
import com.ESI.CareerBooster.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account and returns authentication token"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User registered successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "User already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "User registration information",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RegisterRequest.class),
                examples = @ExampleObject(
                    name = "Register Example",
                    value = "{\"name\": \"John Doe\", \"email\": \"john.doe@example.com\", \"password\": \"password123\"}"
                )
            )
        )
        @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(
        summary = "Login user",
        description = "Authenticates user and returns JWT token. Use this token in the 'Authorize' button at the top of the page for other endpoints."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "User credentials",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginRequest.class),
                examples = @ExampleObject(
                    name = "Login Example",
                    value = "{\"email\": \"user@example.com\", \"password\": \"password123\"}"
                )
            )
        )
        @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }
} 