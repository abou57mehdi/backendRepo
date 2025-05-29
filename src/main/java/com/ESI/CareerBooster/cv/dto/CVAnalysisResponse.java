package com.ESI.CareerBooster.cv.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class CVAnalysisResponse {
    private List<String> skills = new ArrayList<>();
    private List<String> gaps = new ArrayList<>();
    private List<CourseRecommendation> recommendations = new ArrayList<>();
    private String textAnalysis;

    public CVAnalysisResponse() {
        this.recommendations = new ArrayList<>();
    }

    public CVAnalysisResponse(List<CourseRecommendation> recommendations) {
        this.recommendations = recommendations != null ? recommendations : new ArrayList<>();
    }

    public List<CourseRecommendation> getRecommendations() {
        return recommendations != null ? recommendations : new ArrayList<>();
    }

    public void setRecommendations(List<CourseRecommendation> recommendations) {
        this.recommendations = recommendations != null ? recommendations : new ArrayList<>();
    }

    @Data
    public static class CourseRecommendation {
        private String title;
        private String provider;
        private int matchScore;
        private String reason;
        private String skillGapAddressed;
        private String estimatedTimeToComplete;
        private String difficultyLevel;
        private List<String> prerequisites = new ArrayList<>();
        private String careerImpact;
    }
} 