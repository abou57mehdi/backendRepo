-- Enhanced CV Scoring System Migration
-- This migration adds new columns to support the enhanced scoring system

-- Add new columns to cv_scores table if they don't exist
ALTER TABLE cv_scores ADD COLUMN IF NOT EXISTS ats_compatibility_score INTEGER DEFAULT 0;
ALTER TABLE cv_scores ADD COLUMN IF NOT EXISTS career_level VARCHAR(20);
ALTER TABLE cv_scores ADD COLUMN IF NOT EXISTS grade VARCHAR(5);
ALTER TABLE cv_scores ADD COLUMN IF NOT EXISTS industry_benchmark INTEGER;

-- Create score_history table for tracking score improvements over time
CREATE TABLE IF NOT EXISTS score_history (
    id BIGSERIAL PRIMARY KEY,
    user_email VARCHAR(255) NOT NULL,
    cv_id BIGINT REFERENCES cvs(id),
    overall_score INTEGER NOT NULL,
    contact_info_score INTEGER DEFAULT 0,
    summary_score INTEGER DEFAULT 0,
    experience_score INTEGER DEFAULT 0,
    education_score INTEGER DEFAULT 0,
    skills_score INTEGER DEFAULT 0,
    projects_score INTEGER DEFAULT 0,
    formatting_score INTEGER DEFAULT 0,
    keyword_score INTEGER DEFAULT 0,
    ats_compatibility_score INTEGER DEFAULT 0,
    industry_type VARCHAR(50),
    career_level VARCHAR(20),
    grade VARCHAR(5),
    analysis_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    improvements TEXT
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_score_history_user_email ON score_history(user_email);
CREATE INDEX IF NOT EXISTS idx_score_history_analysis_date ON score_history(analysis_date);
CREATE INDEX IF NOT EXISTS idx_score_history_industry_type ON score_history(industry_type);
CREATE INDEX IF NOT EXISTS idx_cv_scores_industry_type ON cv_scores(industry_type);

-- Update existing cv_scores with default values for new columns
UPDATE cv_scores 
SET 
    ats_compatibility_score = COALESCE(ats_compatibility_score, 85),
    career_level = COALESCE(career_level, 'MID_LEVEL'),
    grade = CASE 
        WHEN overall_score >= 90 THEN 'A+'
        WHEN overall_score >= 85 THEN 'A'
        WHEN overall_score >= 80 THEN 'A-'
        WHEN overall_score >= 75 THEN 'B+'
        WHEN overall_score >= 70 THEN 'B'
        WHEN overall_score >= 65 THEN 'B-'
        WHEN overall_score >= 60 THEN 'C+'
        WHEN overall_score >= 55 THEN 'C'
        ELSE 'D'
    END,
    industry_benchmark = CASE 
        WHEN industry_type = 'TECHNOLOGY' THEN 78
        WHEN industry_type = 'MARKETING' THEN 75
        WHEN industry_type = 'FINANCE' THEN 80
        WHEN industry_type = 'HEALTHCARE' THEN 76
        WHEN industry_type = 'EDUCATION' THEN 74
        ELSE 75
    END
WHERE ats_compatibility_score IS NULL 
   OR career_level IS NULL 
   OR grade IS NULL 
   OR industry_benchmark IS NULL;

-- Migrate existing cv_scores to score_history for users who have scores
INSERT INTO score_history (
    user_email, 
    cv_id, 
    overall_score, 
    contact_info_score,
    summary_score,
    experience_score,
    education_score,
    skills_score,
    projects_score,
    formatting_score,
    keyword_score,
    ats_compatibility_score,
    industry_type, 
    career_level,
    grade,
    analysis_date,
    improvements
)
SELECT 
    c.user_email,
    cs.cv_id,
    cs.overall_score,
    COALESCE(cs.contact_info_score, 0),
    COALESCE(cs.summary_score, 0),
    COALESCE(cs.experience_score, 0),
    COALESCE(cs.education_score, 0),
    COALESCE(cs.skills_score, 0),
    COALESCE(cs.projects_score, 0),
    COALESCE(cs.formatting_score, 0),
    COALESCE(cs.keyword_score, 0),
    COALESCE(cs.ats_compatibility_score, 85),
    cs.industry_type,
    cs.career_level,
    cs.grade,
    COALESCE(cs.created_at, CURRENT_TIMESTAMP),
    cs.recommendations
FROM cv_scores cs
JOIN cvs c ON cs.cv_id = c.id
WHERE NOT EXISTS (
    SELECT 1 FROM score_history sh 
    WHERE sh.cv_id = cs.cv_id 
    AND sh.user_email = c.user_email
);

-- Add comments to tables for documentation
COMMENT ON TABLE score_history IS 'Tracks CV score history and improvements over time for analytics';
COMMENT ON COLUMN score_history.user_email IS 'Email of the user who owns this score record';
COMMENT ON COLUMN score_history.overall_score IS 'Overall CV score (0-100)';
COMMENT ON COLUMN score_history.grade IS 'Letter grade based on score (A+, A, A-, B+, etc.)';
COMMENT ON COLUMN score_history.career_level IS 'Detected career level (ENTRY_LEVEL, JUNIOR, MID_LEVEL, SENIOR, EXECUTIVE)';
COMMENT ON COLUMN score_history.ats_compatibility_score IS 'ATS (Applicant Tracking System) compatibility score';

COMMENT ON COLUMN cv_scores.ats_compatibility_score IS 'ATS compatibility score for the CV';
COMMENT ON COLUMN cv_scores.career_level IS 'Detected career level from CV content';
COMMENT ON COLUMN cv_scores.grade IS 'Letter grade representation of the overall score';
COMMENT ON COLUMN cv_scores.industry_benchmark IS 'Industry average score for comparison';
