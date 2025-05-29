package com.ESI.CareerBooster.cv.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Enhanced CV analysis response with detailed scoring breakdown")
public class EnhancedCVAnalysisResponse {
    
    @Schema(description = "Overall CV score (0-100)", example = "85")
    private Integer overallScore;
    
    @Schema(description = "Letter grade based on score", example = "A")
    private String grade;
    
    @Schema(description = "Detailed section scores breakdown")
    private SectionScores sectionScores;
    
    @Schema(description = "Detected industry type", example = "TECHNOLOGY")
    private String industryType;
    
    @Schema(description = "Detected career level", example = "SENIOR")
    private String careerLevel;
    
    @Schema(description = "Personalized improvement recommendations")
    private List<String> recommendations;
    
    @Schema(description = "Industry benchmark score", example = "78")
    private Integer industryBenchmark;
    
    @Schema(description = "Score trend compared to previous analysis", example = "IMPROVING")
    private String trend;
    
    @Schema(description = "Analysis timestamp")
    private LocalDateTime analysisDate;
    
    @Schema(description = "ATS compatibility score", example = "92")
    private Integer atsCompatibilityScore;
    
    @Schema(description = "Missing sections that should be added")
    private List<String> missingSections;
    
    @Schema(description = "Score comparison with industry peers")
    private ScoreComparison scoreComparison;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SectionScores {
        @Schema(description = "Contact information score", example = "15")
        private Integer contactInfo;
        
        @Schema(description = "Professional summary score", example = "12")
        private Integer summary;
        
        @Schema(description = "Work experience score", example = "28")
        private Integer experience;
        
        @Schema(description = "Education score", example = "14")
        private Integer education;
        
        @Schema(description = "Skills score", example = "25")
        private Integer skills;
        
        @Schema(description = "Projects score", example = "8")
        private Integer projects;
        
        @Schema(description = "Formatting and structure score", example = "18")
        private Integer formatting;
        
        @Schema(description = "Industry keywords score", example = "12")
        private Integer keywords;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScoreComparison {
        @Schema(description = "Percentile ranking within industry", example = "75")
        private Integer percentileRank;
        
        @Schema(description = "Points above/below industry average", example = "+7")
        private Integer pointsDifference;
        
        @Schema(description = "Comparison status", example = "ABOVE_AVERAGE")
        private String status; // ABOVE_AVERAGE, AVERAGE, BELOW_AVERAGE
    }
}
