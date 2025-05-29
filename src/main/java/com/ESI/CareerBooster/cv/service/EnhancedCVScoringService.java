package com.ESI.CareerBooster.cv.service;

import com.ESI.CareerBooster.cv.model.CV;
import com.ESI.CareerBooster.cv.model.CVScore;
import com.ESI.CareerBooster.cv.model.ScoreHistory;
import com.ESI.CareerBooster.cv.repository.CVScoreRepository;
import com.ESI.CareerBooster.cv.repository.ScoreHistoryRepository;
import com.ESI.CareerBooster.cv.dto.EnhancedCVAnalysisResponse;
import com.ESI.CareerBooster.cv.dto.ScoreHistoryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnhancedCVScoringService {

    private final CVScoreRepository cvScoreRepository;
    private final ScoreHistoryRepository scoreHistoryRepository;

    // Enhanced industry keywords with weights
    private static final Map<String, Map<String, Integer>> WEIGHTED_INDUSTRY_KEYWORDS = Map.of(
        "TECHNOLOGY", Map.of(
            "java" , 5, "python", 5, "javascript", 5, "react", 4, "spring", 4,
            "docker", 4, "kubernetes", 4, "aws", 5, "microservices", 4, "agile", 3
        ),
        "MARKETING", Map.of(
            "seo", 5, "google analytics", 4, "social media", 3, "content marketing", 4,
            "ppc", 4, "conversion", 4, "brand", 3, "campaign", 3, "roi", 4
        ),
        "FINANCE", Map.of(
            "financial modeling", 5, "excel", 4, "bloomberg", 4, "risk management", 5,
            "portfolio", 4, "derivatives", 4, "compliance", 4, "audit", 3
        )
    );

    // Career level indicators
    private static final Map<String, Integer> CAREER_LEVEL_KEYWORDS = Map.of(
        "intern", 1, "junior", 2, "associate", 3, "senior", 4, "lead", 5,
        "principal", 6, "manager", 5, "director", 7, "vp", 8, "ceo", 10
    );

    // Achievement indicators with weights
    private static final Map<Pattern, Integer> ACHIEVEMENT_PATTERNS = Map.of(
        Pattern.compile("\\b(increased|improved|reduced|saved|generated)\\s+.*?\\b(\\d+)%", Pattern.CASE_INSENSITIVE), 5,
        Pattern.compile("\\$\\s*(\\d+(?:,\\d{3})*(?:\\.\\d{2})?)[kmb]?", Pattern.CASE_INSENSITIVE), 4,
        Pattern.compile("\\b(managed|led|supervised)\\s+.*?(\\d+)\\s+(people|team|employees)", Pattern.CASE_INSENSITIVE), 4,
        Pattern.compile("\\b(award|recognition|certification|patent)", Pattern.CASE_INSENSITIVE), 3
    );

    /**
     * Main method to analyze CV and return enhanced response
     */
    public EnhancedCVAnalysisResponse analyzeCV(MultipartFile file, String userEmail, String industry) {
        try {
            // This would integrate with existing CV upload logic
            // For now, we'll assume CV is already processed
            log.info("Analyzing CV for user: {} with industry: {}", userEmail, industry);

            // TODO: Integrate with existing CVAnalyzerService to get CV content
            // String content = extractContentFromFile(file);
            // CV cv = createCVEntity(file, userEmail, content);

            // For now, return a mock response - this will be integrated with actual CV processing
            return createMockEnhancedResponse();

        } catch (Exception e) {
            log.error("Error analyzing CV for user: {}", userEmail, e);
            throw new RuntimeException("Failed to analyze CV", e);
        }
    }

    /**
     * Get score history for a user
     */
    public ScoreHistoryDTO getScoreHistory(String userEmail, int limit) {
        log.info("Getting score history for user: {} with limit: {}", userEmail, limit);

        List<ScoreHistory> history = scoreHistoryRepository.findRecentByUserEmail(userEmail);
        if (history.isEmpty()) {
            return createEmptyScoreHistory();
        }

        ScoreHistoryDTO dto = new ScoreHistoryDTO();
        ScoreHistory latest = history.get(0);

        dto.setLatestScore(latest.getOverallScore());
        dto.setIndustryType(latest.getIndustryType());
        dto.setCareerLevel(latest.getCareerLevel());
        dto.setCurrentGrade(latest.getGrade());
        dto.setLastAnalysisDate(latest.getAnalysisDate());
        dto.setTotalAnalyses(scoreHistoryRepository.countAnalysesByUserEmail(userEmail));
        dto.setAverageScore(scoreHistoryRepository.getAverageScoreByUserEmail(userEmail));

        // Calculate trend
        if (history.size() >= 2) {
            int current = history.get(0).getOverallScore();
            int previous = history.get(1).getOverallScore();
            dto.setTrend(current > previous ? "IMPROVING" : current < previous ? "DECLINING" : "STABLE");
        } else {
            dto.setTrend("STABLE");
        }

        // Convert history to data points
        List<ScoreHistoryDTO.ScoreDataPoint> dataPoints = history.stream()
            .limit(limit)
            .map(this::convertToDataPoint)
            .toList();
        dto.setScoreHistory(dataPoints);

        // Set industry benchmark
        if (latest.getIndustryType() != null) {
            Double benchmark = scoreHistoryRepository.getIndustryBenchmark(latest.getIndustryType());
            dto.setIndustryBenchmark(benchmark != null ? benchmark.intValue() : 75);
        }

        return dto;
    }

    public CVScore calculateEnhancedScore(CV cv) {
        log.info("Calculating enhanced score for CV: {}", cv.getFileName());

        String content = cv.getContent().toLowerCase();
        CVScore score = new CVScore();
        score.setCv(cv);

        // 1. Basic section analysis
        Map<String, Boolean> sectionsPresent = detectSections(content);
        Map<String, Integer> basicScores = calculateBasicSectionScores(sectionsPresent);

        // 2. Enhanced quality analysis
        int experienceQualityScore = calculateEnhancedExperienceScore(content);
        int skillsRelevanceScore = calculateSkillsRelevanceScore(content, detectIndustry(content));
        int achievementScore = calculateAchievementScore(content);
        int careerProgressionScore = calculateCareerProgressionScore(content);
        int formattingScore = calculateAdvancedFormattingScore(content);
        int atsCompatibilityScore = calculateATSCompatibilityScore(content);

        // 3. Set individual scores
        score.setContactInfoScore(basicScores.get("CONTACT"));
        score.setSummaryScore(basicScores.get("SUMMARY"));
        score.setExperienceScore(Math.min(experienceQualityScore + basicScores.get("EXPERIENCE"), 35));
        score.setEducationScore(basicScores.get("EDUCATION"));
        score.setSkillsScore(Math.min(skillsRelevanceScore + basicScores.get("SKILLS"), 30));
        score.setProjectsScore(basicScores.get("PROJECTS"));
        score.setFormattingScore(formattingScore);
        score.setKeywordScore(achievementScore + careerProgressionScore);

        // 4. Calculate weighted overall score
        int overallScore = calculateWeightedOverallScore(score, atsCompatibilityScore);
        score.setOverallScore(Math.min(overallScore, 100));

        // 5. Set additional enhanced fields
        score.setGrade(calculateGrade(score.getOverallScore()));
        score.setCareerLevel(detectCareerLevel(content));
        score.setAtsCompatibilityScore(atsCompatibilityScore);
        score.setIndustryBenchmark(getIndustryBenchmark(detectIndustry(content)));

        // 6. Generate enhanced recommendations
        score.setRecommendations(generateEnhancedRecommendations(score, content));
        score.setIndustryType(detectIndustry(content));

        CVScore savedScore = cvScoreRepository.save(score);

        // 7. Save to score history for tracking
        if (cv.getUser() != null && cv.getUser().getEmail() != null) {
            ScoreHistory history = ScoreHistory.fromCVScore(savedScore, cv.getUser().getEmail());
            scoreHistoryRepository.save(history);
        }

        return savedScore;
    }

    private int calculateEnhancedExperienceScore(String content) {
        int score = 0;

        // Years of experience detection
        Pattern yearsPattern = Pattern.compile("(\\d+)\\s*(?:\\+)?\\s*years?\\s+(?:of\\s+)?experience", Pattern.CASE_INSENSITIVE);
        Matcher yearsMatcher = yearsPattern.matcher(content);
        if (yearsMatcher.find()) {
            int years = Integer.parseInt(yearsMatcher.group(1));
            score += Math.min(years, 10); // Max 10 points for experience years
        }

        // Job progression (multiple positions)
        long jobCount = Arrays.stream(content.split("\\n"))
            .filter(line -> line.matches(".*\\b(20\\d{2})\\b.*"))
            .count();
        score += Math.min(jobCount * 2, 8); // Max 8 points for job progression

        // Leadership indicators
        String[] leadershipTerms = {"managed", "led", "supervised", "coordinated", "directed"};
        for (String term : leadershipTerms) {
            if (content.contains(term)) {
                score += 2;
                break;
            }
        }

        return Math.min(score, 20);
    }

    private int calculateSkillsRelevanceScore(String content, String industry) {
        Map<String, Integer> industryKeywords = WEIGHTED_INDUSTRY_KEYWORDS.get(industry.toUpperCase());
        if (industryKeywords == null) return 5;

        int totalScore = 0;
        int keywordCount = 0;

        for (Map.Entry<String, Integer> entry : industryKeywords.entrySet()) {
            if (content.contains(entry.getKey().toLowerCase())) {
                totalScore += entry.getValue();
                keywordCount++;
            }
        }

        // Bonus for skill diversity
        if (keywordCount >= 5) totalScore += 5;
        if (keywordCount >= 8) totalScore += 3;

        return Math.min(totalScore, 25);
    }

    private int calculateAchievementScore(String content) {
        int score = 0;

        for (Map.Entry<Pattern, Integer> entry : ACHIEVEMENT_PATTERNS.entrySet()) {
            Matcher matcher = entry.getKey().matcher(content);
            while (matcher.find() && score < 15) {
                score += entry.getValue();
            }
        }

        return Math.min(score, 15);
    }

    private int calculateCareerProgressionScore(String content) {
        int maxLevel = 0;

        for (Map.Entry<String, Integer> entry : CAREER_LEVEL_KEYWORDS.entrySet()) {
            if (content.contains(entry.getKey())) {
                maxLevel = Math.max(maxLevel, entry.getValue());
            }
        }

        return Math.min(maxLevel, 10);
    }

    private int calculateAdvancedFormattingScore(String content) {
        int score = 10; // Base score

        // Length optimization
        int length = content.length();
        if (length >= 800 && length <= 3000) score += 3;
        else if (length < 500 || length > 5000) score -= 5;

        // Structure indicators
        if (content.contains("•") || content.contains("-")) score += 2;
        if (content.split("\\n").length >= 10) score += 2; // Good sectioning

        // Professional formatting indicators
        if (content.matches(".*\\b[A-Z][a-z]+\\s+[A-Z][a-z]+\\b.*")) score += 1; // Proper names
        if (content.matches(".*\\b\\d{4}\\s*-\\s*\\d{4}\\b.*")) score += 2; // Date ranges

        return Math.min(score, 20);
    }

    private int calculateATSCompatibilityScore(String content) {
        int score = 10; // Base ATS score

        // ATS-friendly indicators
        if (!content.contains("@") || !content.contains(".com")) score -= 3; // Missing contact
        if (content.length() < 300) score -= 5; // Too short for ATS
        if (content.matches(".*[^\\x00-\\x7F].*")) score -= 2; // Special characters

        // ATS-friendly structure
        String[] sections = {"experience", "education", "skills", "summary"};
        for (String section : sections) {
            if (content.contains(section)) score += 1;
        }

        return Math.min(score, 15);
    }

    private int calculateWeightedOverallScore(CVScore score, int atsScore) {
        // Weighted calculation based on importance
        double weightedScore =
            (score.getContactInfoScore() * 0.10) +
            (score.getSummaryScore() * 0.15) +
            (score.getExperienceScore() * 0.30) +
            (score.getEducationScore() * 0.10) +
            (score.getSkillsScore() * 0.20) +
            (score.getProjectsScore() * 0.05) +
            (score.getFormattingScore() * 0.05) +
            (score.getKeywordScore() * 0.03) +
            (atsScore * 0.02);

        return (int) Math.round(weightedScore);
    }

    // Helper methods (simplified for brevity)
    private Map<String, Boolean> detectSections(String content) {
        Map<String, Boolean> sections = new HashMap<>();
        sections.put("CONTACT", content.contains("@") || content.contains("phone"));
        sections.put("SUMMARY", content.contains("summary") || content.contains("objective"));
        sections.put("EXPERIENCE", content.contains("experience") || content.contains("work"));
        sections.put("EDUCATION", content.contains("education") || content.contains("degree"));
        sections.put("SKILLS", content.contains("skills") || content.contains("technologies"));
        sections.put("PROJECTS", content.contains("projects") || content.contains("portfolio"));
        return sections;
    }

    private Map<String, Integer> calculateBasicSectionScores(Map<String, Boolean> sections) {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("CONTACT", sections.get("CONTACT") ? 15 : 0);
        scores.put("SUMMARY", sections.get("SUMMARY") ? 15 : 0);
        scores.put("EXPERIENCE", sections.get("EXPERIENCE") ? 25 : 0);
        scores.put("EDUCATION", sections.get("EDUCATION") ? 15 : 0);
        scores.put("SKILLS", sections.get("SKILLS") ? 20 : 0);
        scores.put("PROJECTS", sections.get("PROJECTS") ? 10 : 0);
        return scores;
    }

    private String detectIndustry(String content) {
        for (String industry : WEIGHTED_INDUSTRY_KEYWORDS.keySet()) {
            Map<String, Integer> keywords = WEIGHTED_INDUSTRY_KEYWORDS.get(industry);
            long matchCount = keywords.keySet().stream()
                .mapToLong(keyword -> content.contains(keyword.toLowerCase()) ? 1 : 0)
                .sum();
            if (matchCount >= 3) return industry;
        }
        return "GENERAL";
    }

    private String generateEnhancedRecommendations(CVScore score, String content) {
        StringBuilder recommendations = new StringBuilder();

        if (score.getOverallScore() >= 90) {
            recommendations.append("🌟 Excellent CV! Your profile stands out with strong achievements and relevant skills.\n");
        } else if (score.getOverallScore() >= 75) {
            recommendations.append("✅ Good CV! A few enhancements could make it even stronger:\n");
        } else {
            recommendations.append("🔧 Your CV has potential! Here are key improvements:\n");
        }

        if (score.getExperienceScore() < 25) {
            recommendations.append("• Add quantifiable achievements (e.g., 'Increased sales by 25%')\n");
        }
        if (score.getSkillsScore() < 20) {
            recommendations.append("• Include more industry-relevant technical skills\n");
        }
        if (score.getFormattingScore() < 15) {
            recommendations.append("• Improve formatting with bullet points and clear sections\n");
        }

        return recommendations.toString();
    }

    // Helper methods for the new public API
    private EnhancedCVAnalysisResponse createMockEnhancedResponse() {
        EnhancedCVAnalysisResponse response = new EnhancedCVAnalysisResponse();
        response.setOverallScore(85);
        response.setGrade("A");
        response.setIndustryType("TECHNOLOGY");
        response.setCareerLevel("SENIOR");
        response.setIndustryBenchmark(78);
        response.setTrend("IMPROVING");
        response.setAnalysisDate(LocalDateTime.now());
        response.setAtsCompatibilityScore(92);

        // Section scores
        EnhancedCVAnalysisResponse.SectionScores sectionScores = new EnhancedCVAnalysisResponse.SectionScores();
        sectionScores.setContactInfo(15);
        sectionScores.setSummary(12);
        sectionScores.setExperience(28);
        sectionScores.setEducation(14);
        sectionScores.setSkills(25);
        sectionScores.setProjects(8);
        sectionScores.setFormatting(18);
        sectionScores.setKeywords(12);
        response.setSectionScores(sectionScores);

        // Recommendations
        response.setRecommendations(List.of(
            "Add quantifiable achievements to your experience section",
            "Include more industry-specific technical skills",
            "Consider adding a projects section to showcase your work"
        ));

        // Score comparison
        EnhancedCVAnalysisResponse.ScoreComparison comparison = new EnhancedCVAnalysisResponse.ScoreComparison();
        comparison.setPercentileRank(75);
        comparison.setPointsDifference(7);
        comparison.setStatus("ABOVE_AVERAGE");
        response.setScoreComparison(comparison);

        return response;
    }

    private ScoreHistoryDTO createEmptyScoreHistory() {
        ScoreHistoryDTO dto = new ScoreHistoryDTO();
        dto.setLatestScore(0);
        dto.setAverageScore(0.0);
        dto.setTotalAnalyses(0L);
        dto.setTrend("STABLE");
        dto.setScoreHistory(new ArrayList<>());
        dto.setRecommendations(List.of("Upload your first CV to get started"));
        return dto;
    }

    private ScoreHistoryDTO.ScoreDataPoint convertToDataPoint(ScoreHistory history) {
        ScoreHistoryDTO.ScoreDataPoint point = new ScoreHistoryDTO.ScoreDataPoint();
        point.setDate(history.getAnalysisDate());
        point.setScore(history.getOverallScore());
        point.setGrade(history.getGrade());
        point.setIndustryType(history.getIndustryType());
        return point;
    }

    // Grade calculation helper
    private String calculateGrade(int score) {
        if (score >= 90) return "A+";
        if (score >= 85) return "A";
        if (score >= 80) return "A-";
        if (score >= 75) return "B+";
        if (score >= 70) return "B";
        if (score >= 65) return "B-";
        if (score >= 60) return "C+";
        if (score >= 55) return "C";
        return "D";
    }

    private String detectCareerLevel(String content) {
        int maxLevel = 0;

        for (Map.Entry<String, Integer> entry : CAREER_LEVEL_KEYWORDS.entrySet()) {
            if (content.contains(entry.getKey().toLowerCase())) {
                maxLevel = Math.max(maxLevel, entry.getValue());
            }
        }

        if (maxLevel >= 8) return "EXECUTIVE";
        if (maxLevel >= 6) return "SENIOR";
        if (maxLevel >= 4) return "MID_LEVEL";
        if (maxLevel >= 2) return "JUNIOR";
        return "ENTRY_LEVEL";
    }

    private Integer getIndustryBenchmark(String industry) {
        return switch (industry.toUpperCase()) {
            case "TECHNOLOGY" -> 78;
            case "MARKETING" -> 75;
            case "FINANCE" -> 80;
            case "HEALTHCARE" -> 76;
            case "EDUCATION" -> 74;
            default -> 75;
        };
    }
}
