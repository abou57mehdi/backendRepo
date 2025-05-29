package com.ESI.CareerBooster.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Enumeration;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        log.debug("*** Entering JwtAuthFilter ***");
        log.debug("Request URI: {}", request.getRequestURI());
        log.debug("Request Method: {}", request.getMethod());

        try {
            // Log all request headers for debugging
            log.debug("=== Request Headers ===");
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                log.debug("{}: {}", headerName, request.getHeader(headerName));
            }
            log.debug("=====================");

            final String authHeader = request.getHeader("Authorization");
            log.debug("Processing request to: {} with auth header: {}", request.getRequestURI(), authHeader);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.debug("No valid Authorization header found, proceeding with filter chain");
                filterChain.doFilter(request, response);
                return;
            }

            final String jwt = authHeader.substring(7);
            log.debug("Extracted JWT token: {}", jwt);
            
            final String userEmail = jwtUtil.getEmailFromToken(jwt);
            log.debug("Extracted email from token: {}", userEmail);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                log.debug("Loaded user details for: {}", userEmail);

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Authentication successful for user: {}", userEmail);
                } else {
                    log.warn("Token validation failed for user: {}", userEmail);
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                    return;
                }
            }
        } catch (Exception e) {
            log.error("Error processing JWT token: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
        log.debug("*** Exiting JwtAuthFilter ***");
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        Map<String, String> error = new HashMap<>();
        error.put("error", "Authentication failed");
        error.put("message", message);
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
} 