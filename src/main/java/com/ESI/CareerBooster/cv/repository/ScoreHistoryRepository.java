package com.ESI.CareerBooster.cv.repository;

import com.ESI.CareerBooster.cv.model.ScoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScoreHistoryRepository extends JpaRepository<ScoreHistory, Long> {
    
    /**
     * Find score history for a user, ordered by analysis date descending
     */
    List<ScoreHistory> findByUserEmailOrderByAnalysisDateDesc(String userEmail);
    
    /**
     * Find recent score history for a user with limit
     */
    @Query("SELECT sh FROM ScoreHistory sh WHERE sh.userEmail = :userEmail ORDER BY sh.analysisDate DESC")
    List<ScoreHistory> findRecentByUserEmail(@Param("userEmail") String userEmail);
    
    /**
     * Get score trend for a user (last N scores)
     */
    @Query("SELECT sh FROM ScoreHistory sh WHERE sh.userEmail = :userEmail ORDER BY sh.analysisDate DESC LIMIT :limit")
    List<ScoreHistory> findScoreTrend(@Param("userEmail") String userEmail, @Param("limit") int limit);
    
    /**
     * Get average score for a user
     */
    @Query("SELECT AVG(sh.overallScore) FROM ScoreHistory sh WHERE sh.userEmail = :userEmail")
    Double getAverageScoreByUserEmail(@Param("userEmail") String userEmail);
    
    /**
     * Get latest score for a user
     */
    @Query("SELECT sh FROM ScoreHistory sh WHERE sh.userEmail = :userEmail ORDER BY sh.analysisDate DESC LIMIT 1")
    ScoreHistory getLatestScoreByUserEmail(@Param("userEmail") String userEmail);
    
    /**
     * Get score history within date range
     */
    @Query("SELECT sh FROM ScoreHistory sh WHERE sh.userEmail = :userEmail AND sh.analysisDate BETWEEN :startDate AND :endDate ORDER BY sh.analysisDate DESC")
    List<ScoreHistory> findByUserEmailAndDateRange(
        @Param("userEmail") String userEmail, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Get industry benchmark data
     */
    @Query("SELECT AVG(sh.overallScore) FROM ScoreHistory sh WHERE sh.industryType = :industryType")
    Double getIndustryBenchmark(@Param("industryType") String industryType);
    
    /**
     * Count total analyses for a user
     */
    @Query("SELECT COUNT(sh) FROM ScoreHistory sh WHERE sh.userEmail = :userEmail")
    Long countAnalysesByUserEmail(@Param("userEmail") String userEmail);
    
    /**
     * Get score improvement over time
     */
    @Query("SELECT sh.overallScore FROM ScoreHistory sh WHERE sh.userEmail = :userEmail ORDER BY sh.analysisDate ASC")
    List<Integer> getScoreProgressionByUserEmail(@Param("userEmail") String userEmail);
}
