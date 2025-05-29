package com.ESI.CareerBooster.config;

import com.google.cloud.vertexai.VertexAI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;

@Configuration
public class GeminiConfig {

    @Value("${google.cloud.project-id}")
    private String projectId;

    @Value("${google.cloud.location}")
    private String location;

    @Bean
    public VertexAI vertexAI() throws IOException {
        return new VertexAI(projectId, location);
    }
} 