package com.ESI.CareerBooster.cv.service;

import com.ESI.CareerBooster.cv.dto.CVAnalysisResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {
    
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    
    @Value("${spring.ai.gemini.api-key}")
    private String apiKey;
    
    public CVAnalysisResponse analyzeCV(String cvContent) {
        log.debug("Analyzing CV content with Gemini AI for course recommendations");
        
        try {
            String promptText = constructPrompt(cvContent);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", promptText);
            content.put("parts", List.of(part));
            requestBody.put("contents", List.of(content));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            log.debug("Sending request to Gemini AI service with prompt (courses): {}", requestBody);
            
            try {
                Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
                log.debug("Raw AI response (courses): {}", response);

                if (response == null) {
                    log.error("Received null response from AI service (courses)");
                    throw new RuntimeException("No response received from AI service (courses)");
                }

                String rawApiResponse = extractTextFromResponse(response);
                CVAnalysisResponse analysisResponse = new CVAnalysisResponse();

                int jsonStartIndex = rawApiResponse.indexOf("{");
                int jsonEndIndex = rawApiResponse.lastIndexOf("}");

                if (jsonStartIndex != -1 && jsonEndIndex != -1 && jsonEndIndex > jsonStartIndex) {
                    String jsonSubstring = rawApiResponse.substring(jsonStartIndex, jsonEndIndex + 1);
                    try {
                        JsonNode structuredDataNode = objectMapper.readTree(jsonSubstring);

                        if (structuredDataNode.has("skills")) {
                            structuredDataNode.path("skills").forEach(skill -> analysisResponse.getSkills().add(skill.asText()));
                        }
                        if (structuredDataNode.has("gaps")) {
                            structuredDataNode.path("gaps").forEach(gap -> analysisResponse.getGaps().add(gap.asText()));
                        }
                        if (structuredDataNode.has("recommendations") && structuredDataNode.path("recommendations").isArray()) {
                            List<CVAnalysisResponse.CourseRecommendation> courseRecommendations = objectMapper.readValue(
                                structuredDataNode.path("recommendations").traverse(),
                                objectMapper.getTypeFactory().constructCollectionType(List.class, CVAnalysisResponse.CourseRecommendation.class)
                            );
                            analysisResponse.setRecommendations(courseRecommendations);
                        }

                        analysisResponse.setTextAnalysis(rawApiResponse.substring(0, jsonStartIndex) + rawApiResponse.substring(jsonEndIndex + 1));

                    } catch (Exception e) {
                        log.warn("Could not parse embedded JSON from AI response content (courses): {}", e.getMessage());
                        analysisResponse.setTextAnalysis(rawApiResponse);
                    }
                } else {
                    analysisResponse.setTextAnalysis(rawApiResponse);
                }

                return analysisResponse;
            } catch (Exception e) {
                log.error("Error calling Gemini API (courses): {}", e.getMessage(), e);
                throw new RuntimeException("Failed to call Gemini API (courses): " + e.getMessage(), e);
            }

        } catch (Exception e) {
            log.error("Error analyzing CV (courses): {}", e.getMessage(), e);
            throw new RuntimeException("Failed to analyze CV (courses): " + e.getMessage(), e);
        }
    }

    public String analyzeCVGeneral(String cvContent) {
        log.debug("Analyzing CV content with Gemini AI for general analysis");

        try {
            String promptText = constructGeneralAnalysisPrompt(cvContent);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", promptText);
            content.put("parts", List.of(part));
            requestBody.put("contents", List.of(content));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            log.debug("Sending request to Gemini AI service with prompt (general): {}", requestBody);

            try {
                Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
                log.debug("Raw AI response (general): {}", response);

                if (response == null) {
                    log.error("Received null response from AI service (general)");
                    throw new RuntimeException("No response received from AI service (general)");
                }

                return extractTextFromResponse(response);

            } catch (Exception e) {
                log.error("Error calling Gemini API (general): {}", e.getMessage(), e);
                throw new RuntimeException("Failed to call Gemini API (general): " + e.getMessage(), e);
            }

        } catch (Exception e) {
            log.error("Error analyzing CV (general): {}", e.getMessage(), e);
            throw new RuntimeException("Failed to analyze CV (general): " + e.getMessage(), e);
        }
    }

    private String extractTextFromResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> candidate = candidates.get(0);
                Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                if (content != null) {
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }
            throw new RuntimeException("Could not extract text from Gemini response");
        } catch (Exception e) {
            throw new RuntimeException("Error parsing Gemini response: " + e.getMessage(), e);
        }
    }

    private String constructPrompt(String cvText) {
        return String.format("""
            You are a career development AI assistant. Analyze the following CV to identify relevant skills and potential areas for growth.

            Based ONLY on this analysis, provide a list of highly relevant online course recommendations. Each recommendation should be presented on a new line, prefixed by "- ", followed by the course title, and a brief one-sentence explanation of why it's recommended based on the CV content.

            Do NOT include any introductory text, concluding remarks, section headers (like "Overall Impression", "Skills", "Gaps", "Recommended Courses"), bullet points for skills or gaps, or any information other than the formatted list of course recommendations.

            Your response must follow this exact format:
            - [Course Title]: [Brief explanation based on CV]
            - [Course Title]: [Brief explanation based on CV]
            ...

            CV Text:
            %s
            """, cvText);
    }

    private String constructGeneralAnalysisPrompt(String cvText) {
        return String.format("""
            You are a professional CV reviewer. Analyze the provided CV and provide feedback in the following structured format:

            1. STRENGTHS
            - List 2-3 key positive aspects of the CV
            - Focus on content, presentation, and unique elements
            - Use clear, professional language

            2. IMPROVEMENT AREAS
            - List 3-5 specific areas that need improvement
            - Be specific and actionable
            - Focus on format, content, and presentation
            - Use professional, constructive language

            3. RECOMMENDATIONS
            - Provide 4-5 specific, actionable recommendations
            - Include format suggestions
            - Include content improvements
            - Include presentation tips

            Guidelines for your response:
            - Use clear, professional language
            - Avoid markdown symbols (*, **, etc.)
            - Be specific and actionable
            - Focus on constructive feedback
            - Keep each point concise and clear
            - Use bullet points for better readability
            - Maintain a professional tone throughout

            Format your response exactly like this example:

            STRENGTHS
            • Clear and concise professional summary
            • Well-organized work experience section
            • Strong technical skills presentation

            IMPROVEMENT AREAS
            • Add quantifiable achievements to work experience
            • Include relevant certifications
            • Expand on project descriptions

            RECOMMENDATIONS
            • Restructure work experience to highlight achievements
            • Add a skills section with proficiency levels
            • Include relevant certifications and training
            • Add a professional summary at the top

            Remember to maintain this exact format and structure in your response.

            CV Text:
            %s
            """, cvText);
    }
} 