package com.ESI.CareerBooster.cv.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "score_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_email", nullable = false)
    private String userEmail;
    
    @ManyToOne
    @JoinColumn(name = "cv_id")
    private CV cv;
    
    @Column(name = "overall_score", nullable = false)
    private Integer overallScore;
    
    @Column(name = "contact_info_score")
    private Integer contactInfoScore;
    
    @Column(name = "summary_score")
    private Integer summaryScore;
    
    @Column(name = "experience_score")
    private Integer experienceScore;
    
    @Column(name = "education_score")
    private Integer educationScore;
    
    @Column(name = "skills_score")
    private Integer skillsScore;
    
    @Column(name = "projects_score")
    private Integer projectsScore;
    
    @Column(name = "formatting_score")
    private Integer formattingScore;
    
    @Column(name = "keyword_score")
    private Integer keywordScore;
    
    @Column(name = "ats_compatibility_score")
    private Integer atsCompatibilityScore;
    
    @Column(name = "industry_type")
    private String industryType;
    
    @Column(name = "career_level")
    private String careerLevel;
    
    @Column(name = "grade")
    private String grade;
    
    @Column(name = "analysis_date", nullable = false)
    private LocalDateTime analysisDate;
    
    @Column(name = "improvements", columnDefinition = "TEXT")
    private String improvements;
    
    @PrePersist
    protected void onCreate() {
        if (analysisDate == null) {
            analysisDate = LocalDateTime.now();
        }
    }
    
    // Helper method to create from CVScore
    public static ScoreHistory fromCVScore(CVScore cvScore, String userEmail) {
        ScoreHistory history = new ScoreHistory();
        history.setUserEmail(userEmail);
        history.setCv(cvScore.getCv());
        history.setOverallScore(cvScore.getOverallScore());
        history.setContactInfoScore(cvScore.getContactInfoScore());
        history.setSummaryScore(cvScore.getSummaryScore());
        history.setExperienceScore(cvScore.getExperienceScore());
        history.setEducationScore(cvScore.getEducationScore());
        history.setSkillsScore(cvScore.getSkillsScore());
        history.setProjectsScore(cvScore.getProjectsScore());
        history.setFormattingScore(cvScore.getFormattingScore());
        history.setKeywordScore(cvScore.getKeywordScore());
        history.setAtsCompatibilityScore(cvScore.getAtsCompatibilityScore());
        history.setIndustryType(cvScore.getIndustryType());
        history.setCareerLevel(cvScore.getCareerLevel());
        history.setGrade(cvScore.getGrade());
        history.setAnalysisDate(LocalDateTime.now());
        history.setImprovements(cvScore.getRecommendations());
        return history;
    }
}
