package com.ESI.CareerBooster.cv.service;

import com.ESI.CareerBooster.cv.model.CV;
import com.ESI.CareerBooster.cv.model.CVScore;
import com.ESI.CareerBooster.cv.repository.CVScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CVAnalyzerService {
    
    private final CVScoreRepository cvScoreRepository;
    
    // Industry keywords for detection
    private static final Map<String, List<String>> INDUSTRY_KEYWORDS = Map.of(
        "TECHNOLOGY", Arrays.asList("java", "python", "javascript", "react", "angular", "spring", "docker", "kubernetes", "aws", "azure", "software", "developer", "engineer", "programming", "coding", "database", "api", "frontend", "backend", "fullstack"),
        "FINANCE", Arrays.asList("finance", "banking", "investment", "accounting", "financial", "analyst", "portfolio", "risk", "trading", "economics", "budget", "audit", "compliance", "treasury"),
        "MARKETING", Arrays.asList("marketing", "digital", "social media", "seo", "sem", "content", "brand", "campaign", "analytics", "advertising", "promotion", "market research"),
        "HEALTHCARE", Arrays.asList("healthcare", "medical", "nurse", "doctor", "patient", "clinical", "hospital", "pharmacy", "therapy", "diagnosis", "treatment", "medicine"),
        "EDUCATION", Arrays.asList("education", "teaching", "teacher", "professor", "curriculum", "student", "learning", "academic", "research", "university", "school"),
        "GENERAL", Arrays.asList("management", "leadership", "communication", "teamwork", "project", "analysis", "problem solving", "customer service")
    );
    
    // Section detection patterns
    private static final Map<String, Pattern> SECTION_PATTERNS = Map.of(
        "CONTACT", Pattern.compile("(?i)(contact|email|phone|address|linkedin|github)", Pattern.CASE_INSENSITIVE),
        "SUMMARY", Pattern.compile("(?i)(summary|profile|objective|about|overview)", Pattern.CASE_INSENSITIVE),
        "EXPERIENCE", Pattern.compile("(?i)(experience|work|employment|career|professional|job)", Pattern.CASE_INSENSITIVE),
        "EDUCATION", Pattern.compile("(?i)(education|academic|degree|university|college|school|qualification)", Pattern.CASE_INSENSITIVE),
        "SKILLS", Pattern.compile("(?i)(skills|technical|competencies|abilities|expertise|technologies)", Pattern.CASE_INSENSITIVE),
        "PROJECTS", Pattern.compile("(?i)(projects|portfolio|achievements|accomplishments)", Pattern.CASE_INSENSITIVE)
    );
    
    public CVScore analyzeCV(CV cv) {
        log.info("Analyzing CV: {} for user: {}", cv.getFileName(), cv.getUser().getEmail());
        
        String content = cv.getContent().toLowerCase();
        
        // Check if score already exists
        Optional<CVScore> existingScore = cvScoreRepository.findByCv(cv);
        CVScore score = existingScore.orElse(new CVScore());
        score.setCv(cv);
        
        // Detect industry
        String industry = detectIndustry(content);
        score.setIndustryType(industry);
        
        // Analyze sections
        Map<String, Boolean> sectionsPresent = detectSections(content);
        Map<String, Integer> sectionScores = calculateSectionScores(content, sectionsPresent, industry);
        
        // Set individual scores
        score.setContactInfoScore(sectionScores.get("CONTACT"));
        score.setSummaryScore(sectionScores.get("SUMMARY"));
        score.setExperienceScore(sectionScores.get("EXPERIENCE"));
        score.setEducationScore(sectionScores.get("EDUCATION"));
        score.setSkillsScore(sectionScores.get("SKILLS"));
        score.setProjectsScore(sectionScores.get("PROJECTS"));
        score.setFormattingScore(calculateFormattingScore(content));
        score.setKeywordScore(calculateKeywordScore(content, industry));
        
        // Calculate overall score
        int overallScore = calculateOverallScore(sectionScores, score.getFormattingScore(), score.getKeywordScore());
        score.setOverallScore(overallScore);
        
        // Generate missing sections and recommendations
        List<String> missingSections = findMissingSections(sectionsPresent);
        score.setMissingSections(String.join(", ", missingSections));
        score.setRecommendations(generateRecommendations(sectionsPresent, sectionScores, industry));
        
        CVScore savedScore = cvScoreRepository.save(score);
        log.info("CV analysis completed. Overall score: {} for CV: {}", overallScore, cv.getFileName());
        
        return savedScore;
    }
    
    private String detectIndustry(String content) {
        Map<String, Integer> industryScores = new HashMap<>();
        
        for (Map.Entry<String, List<String>> entry : INDUSTRY_KEYWORDS.entrySet()) {
            String industry = entry.getKey();
            List<String> keywords = entry.getValue();
            
            int score = 0;
            for (String keyword : keywords) {
                if (content.contains(keyword.toLowerCase())) {
                    score++;
                }
            }
            industryScores.put(industry, score);
        }
        
        return industryScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("GENERAL");
    }
    
    private Map<String, Boolean> detectSections(String content) {
        Map<String, Boolean> sections = new HashMap<>();
        
        for (Map.Entry<String, Pattern> entry : SECTION_PATTERNS.entrySet()) {
            String section = entry.getKey();
            Pattern pattern = entry.getValue();
            sections.put(section, pattern.matcher(content).find());
        }
        
        return sections;
    }
    
    private Map<String, Integer> calculateSectionScores(String content, Map<String, Boolean> sectionsPresent, String industry) {
        Map<String, Integer> scores = new HashMap<>();
        
        // Base scores for presence
        scores.put("CONTACT", sectionsPresent.get("CONTACT") ? 15 : 0);
        scores.put("SUMMARY", sectionsPresent.get("SUMMARY") ? 15 : 0);
        scores.put("EXPERIENCE", sectionsPresent.get("EXPERIENCE") ? 25 : 0);
        scores.put("EDUCATION", sectionsPresent.get("EDUCATION") ? 15 : 0);
        scores.put("SKILLS", sectionsPresent.get("SKILLS") ? 20 : 0);
        scores.put("PROJECTS", sectionsPresent.get("PROJECTS") ? 10 : 0);
        
        // Bonus points for content quality
        if (sectionsPresent.get("EXPERIENCE")) {
            scores.put("EXPERIENCE", scores.get("EXPERIENCE") + calculateExperienceQuality(content));
        }
        
        if (sectionsPresent.get("SKILLS")) {
            scores.put("SKILLS", scores.get("SKILLS") + calculateSkillsQuality(content, industry));
        }
        
        return scores;
    }
    
    private int calculateExperienceQuality(String content) {
        int quality = 0;
        
        // Check for years/dates
        if (content.matches(".*\\b(20\\d{2}|19\\d{2})\\b.*")) quality += 3;
        
        // Check for action verbs
        String[] actionVerbs = {"developed", "managed", "led", "created", "implemented", "designed", "improved", "achieved"};
        for (String verb : actionVerbs) {
            if (content.contains(verb)) {
                quality += 1;
                break;
            }
        }
        
        // Check for quantifiable achievements
        if (content.matches(".*\\b\\d+%\\b.*") || content.matches(".*\\$\\d+.*")) quality += 2;
        
        return Math.min(quality, 5); // Max 5 bonus points
    }
    
    private int calculateSkillsQuality(String content, String industry) {
        List<String> industryKeywords = INDUSTRY_KEYWORDS.get(industry);
        if (industryKeywords == null) return 0;
        
        int relevantSkills = 0;
        for (String keyword : industryKeywords) {
            if (content.contains(keyword.toLowerCase())) {
                relevantSkills++;
            }
        }
        
        return Math.min(relevantSkills / 2, 5); // Max 5 bonus points
    }
    
    private int calculateFormattingScore(String content) {
        int score = 10; // Base score
        
        // Check length (not too short, not too long)
        int length = content.length();
        if (length < 500) score -= 3;
        else if (length > 5000) score -= 2;
        
        // Check for structure indicators
        if (content.contains("\n") || content.contains("•") || content.contains("-")) score += 2;
        
        return Math.max(0, Math.min(score, 15));
    }
    
    private int calculateKeywordScore(String content, String industry) {
        List<String> keywords = INDUSTRY_KEYWORDS.get(industry);
        if (keywords == null) return 5;
        
        int keywordCount = 0;
        for (String keyword : keywords) {
            if (content.contains(keyword.toLowerCase())) {
                keywordCount++;
            }
        }
        
        return Math.min(keywordCount, 10); // Max 10 points
    }
    
    private int calculateOverallScore(Map<String, Integer> sectionScores, int formattingScore, int keywordScore) {
        int total = sectionScores.values().stream().mapToInt(Integer::intValue).sum();
        total += formattingScore + keywordScore;
        return Math.min(total, 100); // Cap at 100
    }
    
    private List<String> findMissingSections(Map<String, Boolean> sectionsPresent) {
        List<String> missing = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : sectionsPresent.entrySet()) {
            if (!entry.getValue()) {
                missing.add(entry.getKey());
            }
        }
        return missing;
    }
    
    private String generateRecommendations(Map<String, Boolean> sectionsPresent, Map<String, Integer> sectionScores, String industry) {
        StringBuilder recommendations = new StringBuilder();
        
        if (!sectionsPresent.get("SUMMARY")) {
            recommendations.append("• Add a professional summary section to highlight your key qualifications.\n");
        }
        
        if (!sectionsPresent.get("SKILLS")) {
            recommendations.append("• Include a skills section with relevant ").append(industry.toLowerCase()).append(" technologies.\n");
        }
        
        if (!sectionsPresent.get("PROJECTS")) {
            recommendations.append("• Add a projects section to showcase your practical experience.\n");
        }
        
        if (sectionScores.get("EXPERIENCE") < 20) {
            recommendations.append("• Enhance your experience section with quantifiable achievements and action verbs.\n");
        }
        
        if (recommendations.length() == 0) {
            recommendations.append("• Your CV looks comprehensive! Consider updating it regularly with new achievements.");
        }
        
        return recommendations.toString();
    }
}
