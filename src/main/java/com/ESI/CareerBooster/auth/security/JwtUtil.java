package com.ESI.CareerBooster.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Base64;
import java.util.Arrays;

@Slf4j
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpirationMs;
    
    private SecretKey key;

    public JwtUtil(@Value("${jwt.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
        log.debug("JWT Secret (string): {}", jwtSecret);
        // Decode the hex string to bytes
        byte[] keyBytes = hexStringToByteArray(jwtSecret);
        log.debug("JWT Secret (bytes length): {}", keyBytes.length);
        log.debug("JWT Secret (bytes preview): {}", Arrays.copyOfRange(keyBytes, 0, Math.min(keyBytes.length, 16))); // Log first 16 bytes

        this.key = Keys.hmacShaKeyFor(keyBytes);
        log.debug("JWT Secret (SecretKey algorithm): {}", key.getAlgorithm());
        log.debug("JWT Secret (SecretKey format): {}", key.getFormat());
        log.debug("JWT Secret (SecretKey bytes preview): {}", Arrays.copyOfRange(key.getEncoded(), 0, Math.min(key.getEncoded().length, 16))); // Log first 16 bytes
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public String generateToken(String email) {
        try {
            log.debug("Generating token for email: {}", email);
            String token = Jwts.builder()
                    .setSubject(email)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
            log.debug("Generated token: {}", token);
            return token;
        } catch (Exception e) {
            log.error("Error generating token: {}", e.getMessage());
            throw new RuntimeException("Error generating token", e);
        }
    }

    public String getEmailFromToken(String token) {
        try {
            log.debug("Attempting to parse token: {}", token);
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            log.error("Token expired: {}", e.getMessage());
            throw new RuntimeException("Token expired", e);
        } catch (JwtException e) {
            log.error("Error parsing token: {}", e.getMessage(), e); // Log exception detail
            throw new RuntimeException("Invalid token", e);
        }
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            log.debug("Validating token for user: {}", userDetails.getUsername());
            String email = getEmailFromToken(token);
            boolean isValid = email.equals(userDetails.getUsername()) && !isTokenExpired(token);
            if (!isValid) {
                log.warn("Token validation failed for user: {}", userDetails.getUsername());
            }
            log.debug("Token validation result: {}", isValid);
            return isValid;
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage(), e); // Log exception detail
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            log.debug("Checking token expiration for token: {}", token);
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            boolean expired = expiration.before(new Date());
            log.debug("Token expiration check result: {}", expired);
            return expired;
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage(), e); // Log exception detail
            return true;
        }
    }
} 