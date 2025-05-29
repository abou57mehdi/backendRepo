package com.ESI.CareerBooster.cv.dto;

import lombok.Data;
import java.util.List;
import java.time.LocalDateTime;

@Data
public class HomeScreenData {
    private UserProgress userProgress;
    private List<RecentAnalysis> recentAnalyses;
    private List<CourseRecommendation> courseRecommendations;
    private CVStatus cvStatus;

    @Data
    public static class UserProgress {
        private int completedAnalyses;
        private int completedCourses;
        private int skillScore;
        private LocalDateTime lastAnalysisDate;
        private String industryType;
        private Double averageScore;
        private Integer latestScore;
    }

    @Data
    public static class RecentAnalysis {
        private String id;
        private String fileName;
        private LocalDateTime analysisDate;
        private String summary;
        private List<AnalysisPoint> strengths;
        private List<AnalysisPoint> improvements;
    }

    @Data
    public static class AnalysisPoint {
        private String title;
        private String description;
        private String icon;
    }

    @Data
    public static class CVStatus {
        private String status; // "PENDING", "COMPLETED", "ERROR"
        private String message;
        private LocalDateTime lastUpdate;
    }
}