package com.ESI.CareerBooster.cv.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Score history and analytics data")
public class ScoreHistoryDTO {
    
    @Schema(description = "Latest overall score", example = "85")
    private Integer latestScore;
    
    @Schema(description = "Average score across all analyses", example = "78.5")
    private Double averageScore;
    
    @Schema(description = "Industry type", example = "TECHNOLOGY")
    private String industryType;
    
    @Schema(description = "Industry benchmark score", example = "78")
    private Integer industryBenchmark;
    
    @Schema(description = "Score trend", example = "IMPROVING")
    private String trend;
    
    @Schema(description = "Last analysis date")
    private LocalDateTime lastAnalysisDate;
    
    @Schema(description = "Historical score data points")
    private List<ScoreDataPoint> scoreHistory;
    
    @Schema(description = "Latest recommendations")
    private List<String> recommendations;
    
    @Schema(description = "Total number of analyses performed", example = "5")
    private Long totalAnalyses;
    
    @Schema(description = "Score improvement since first analysis", example = "+15")
    private Integer improvementSinceFirst;
    
    @Schema(description = "Current grade", example = "A")
    private String currentGrade;
    
    @Schema(description = "Career level", example = "SENIOR")
    private String careerLevel;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScoreDataPoint {
        @Schema(description = "Analysis date")
        private LocalDateTime date;
        
        @Schema(description = "Overall score at this point", example = "82")
        private Integer score;
        
        @Schema(description = "Grade at this point", example = "A-")
        private String grade;
        
        @Schema(description = "Industry type at analysis", example = "TECHNOLOGY")
        private String industryType;
    }
}
