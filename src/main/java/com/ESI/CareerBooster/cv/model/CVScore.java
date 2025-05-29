package com.ESI.CareerBooster.cv.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cv_scores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CVScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "cv_id", nullable = false)
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

    @Column(name = "industry_type")
    private String industryType;

    @Column(name = "missing_sections", columnDefinition = "TEXT")
    private String missingSections;

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    // Additional enhanced fields
    @Column(name = "ats_compatibility_score")
    private Integer atsCompatibilityScore = 0;

    @Column(name = "career_level")
    private String careerLevel;

    @Column(name = "grade")
    private String grade;

    @Column(name = "industry_benchmark")
    private Integer industryBenchmark;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
