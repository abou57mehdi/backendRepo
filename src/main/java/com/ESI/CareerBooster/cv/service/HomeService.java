package com.ESI.CareerBooster.cv.service;

import com.ESI.CareerBooster.cv.dto.HomeScreenData;
import com.ESI.CareerBooster.cv.repository.CVRepository;
import com.ESI.CareerBooster.auth.repository.UserRepository;
import com.ESI.CareerBooster.cv.model.CV;
import com.ESI.CareerBooster.cv.model.CVScore;
import com.ESI.CareerBooster.cv.repository.CVScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeService {

    private final CVRepository cvRepository;
    private final UserRepository userRepository;
    private final CVScoreRepository cvScoreRepository;

    public HomeScreenData getHomeData(String userEmail) {
        log.debug("Fetching home data for user: {}", userEmail);

        HomeScreenData homeData = new HomeScreenData();

        // Get user's CV analyses
        List<CV> userCVs = cvRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);

        // Set user progress
        HomeScreenData.UserProgress progress = new HomeScreenData.UserProgress();
        progress.setCompletedAnalyses(userCVs.size());
        progress.setCompletedCourses(0); // TODO: Implement course completion tracking
        progress.setSkillScore(calculateSkillScore(userCVs));
        progress.setLastAnalysisDate(
            userCVs.isEmpty() ? LocalDateTime.now() : userCVs.get(0).getCreatedAt()
        );

        // Add enhanced score data
        if (!userCVs.isEmpty()) {
            Double averageScore = cvScoreRepository.getAverageScoreByUserEmail(userEmail);
            progress.setAverageScore(averageScore);

            Optional<CVScore> latestScore = cvScoreRepository.getLatestScoreByUserEmail(userEmail);
            if (latestScore.isPresent()) {
                progress.setLatestScore(latestScore.get().getOverallScore());
                progress.setIndustryType(latestScore.get().getIndustryType());
            }
        }

        homeData.setUserProgress(progress);

        // Set recent analyses
        if (!userCVs.isEmpty()) {
            List<HomeScreenData.RecentAnalysis> recentAnalyses = userCVs.stream()
                .limit(1) // Only get the most recent analysis
                .map(this::convertToRecentAnalysis)
                .collect(Collectors.toList());
            homeData.setRecentAnalyses(recentAnalyses);
        }

        // Set CV status
        HomeScreenData.CVStatus cvStatus = new HomeScreenData.CVStatus();
        cvStatus.setStatus(userCVs.isEmpty() ? "PENDING" : "COMPLETED");
        cvStatus.setMessage(userCVs.isEmpty() ? "Upload your first CV" : "Analysis complete");
        cvStatus.setLastUpdate(
            userCVs.isEmpty() ? LocalDateTime.now() : userCVs.get(0).getCreatedAt()
        );
        homeData.setCvStatus(cvStatus);

        return homeData;
    }

    private HomeScreenData.RecentAnalysis convertToRecentAnalysis(CV cv) {
        HomeScreenData.RecentAnalysis analysis = new HomeScreenData.RecentAnalysis();
        analysis.setId(cv.getId().toString());
        analysis.setFileName(cv.getFileName());
        analysis.setAnalysisDate(cv.getCreatedAt());
        analysis.setSummary("Recent CV Analysis");

        // Parse recommendations into strengths and improvements
        String[] recommendations = cv.getRecommendations().split("\n");
        List<HomeScreenData.AnalysisPoint> strengths = new ArrayList<>();
        List<HomeScreenData.AnalysisPoint> improvements = new ArrayList<>();

        boolean isStrength = true;
        for (String rec : recommendations) {
            if (rec.trim().isEmpty()) continue;

            HomeScreenData.AnalysisPoint point = new HomeScreenData.AnalysisPoint();

            // Safe parsing - handle cases where there's no colon
            String[] parts = rec.split(":", 2); // Limit to 2 parts
            if (parts.length >= 2) {
                point.setTitle(parts[0].trim());
                point.setDescription(parts[1].trim());
            } else {
                // If no colon, use the whole text as title
                point.setTitle(rec.trim());
                point.setDescription("Analysis point");
            }

            point.setIcon(isStrength ? "💪" : "🎯");

            if (isStrength) {
                strengths.add(point);
            } else {
                improvements.add(point);
            }

            isStrength = !isStrength;
        }

        // Ensure we have at least some default data if parsing fails
        if (strengths.isEmpty() && improvements.isEmpty()) {
            HomeScreenData.AnalysisPoint defaultPoint = new HomeScreenData.AnalysisPoint();
            defaultPoint.setTitle("CV Analysis Complete");
            defaultPoint.setDescription("Your CV has been analyzed successfully");
            defaultPoint.setIcon("📄");
            strengths.add(defaultPoint);
        }

        analysis.setStrengths(strengths);
        analysis.setImprovements(improvements);

        return analysis;
    }

    private int calculateSkillScore(List<CV> cvs) {
        if (cvs.isEmpty()) {
            return 0;
        }

        // Get the average score from all CV scores for this user
        Double averageScore = cvScoreRepository.getAverageScoreByUserEmail(cvs.get(0).getUser().getEmail());

        if (averageScore != null) {
            return averageScore.intValue();
        }

        // Fallback to simple calculation if no scores exist yet
        return Math.min(cvs.size() * 10, 100);
    }
}