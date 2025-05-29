package com.ESI.CareerBooster.courses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Base64;
import java.util.Map;

@Service
public class CourseraService {
    private static final Logger logger = LoggerFactory.getLogger(CourseraService.class);
    // Use provided client key and secret
    private final String clientKey = "enngROFyADaGCGCFHdyX4AlcNL6Qn7895f2giuJZIFmYAI87";
    private final String clientSecret = "AZOMPKiYyOkNdCiV9g11841wTGNZkzhAqpr2VHa93OCcKydHso92XypAVrGaiwNC";

    private final String TOKEN_URL = "https://api.coursera.org/oauth2/client_credentials/token";
    private final String BASE_URL = "https://api.coursera.org/api/courses.v1";

    private String accessToken = null;
    private long tokenExpiry = 0;

    private String getAccessToken() {
        if (accessToken != null && System.currentTimeMillis() < tokenExpiry) {
            logger.debug("Using cached Coursera access token: {}", accessToken);
            System.out.println("[CourseraService] Using cached access token: " + accessToken);
            return accessToken;
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        String creds = clientKey + ":" + clientSecret;
        String base64Creds = Base64.getEncoder().encodeToString(creds.getBytes());
        headers.set("Authorization", "Basic " + base64Creds);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            logger.debug("Requesting Coursera access token with clientKey: {}", clientKey);
            System.out.println("[CourseraService] Requesting access token from: " + TOKEN_URL);
            System.out.println("[CourseraService] Headers: " + headers);
            System.out.println("[CourseraService] Body: " + body);
            ResponseEntity<Map> response = restTemplate.postForEntity(TOKEN_URL, request, Map.class);
            logger.info("Coursera token response: {}", response);
            System.out.println("[CourseraService] Token response: " + response);
            Map<String, Object> respBody = response.getBody();
            if (respBody != null) {
                accessToken = (String) respBody.get("access_token");
                Object expiresObj = respBody.get("expires_in");
                int expiresIn = expiresObj instanceof Integer ? (Integer) expiresObj : Integer.parseInt(expiresObj.toString());
                tokenExpiry = System.currentTimeMillis() + (expiresIn - 60) * 1000; // buffer 1 min
                logger.debug("Obtained Coursera access token: {} (expires in {}s)", accessToken, expiresIn);
                System.out.println("[CourseraService] Obtained access token: " + accessToken + ", expires in: " + expiresIn + "s");
            } else {
                logger.error("Token response body is null");
                System.out.println("[CourseraService] Token response body is null");
            }
            return accessToken;
        } catch (Exception e) {
            logger.error("Failed to get Coursera access token", e);
            System.out.println("[CourseraService] Failed to get access token: " + e.getMessage());
            throw new RuntimeException("Failed to get Coursera access token: " + e.getMessage());
        }
    }

    public String fetchCoursesByCategory(String category) {
        String token = getAccessToken();
        RestTemplate restTemplate = new RestTemplate();
        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("q", "search")
                .queryParam("query", category)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Accept", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            logger.debug("Requesting Coursera courses with token: {}", token);
            System.out.println("[CourseraService] Requesting courses from: " + url);
            System.out.println("[CourseraService] Headers: " + headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            logger.info("Coursera API response: {}", response);
            System.out.println("[CourseraService] Courses API response: " + response);
            System.out.println("[CourseraService] Courses API response body: " + response.getBody());
            return response.getBody();
        } catch (Exception e) {
            logger.error("Coursera API error", e);
            System.out.println("[CourseraService] Coursera API error: " + e.getMessage());
            return "{\"elements\":[],\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}";
        }
    }
} 