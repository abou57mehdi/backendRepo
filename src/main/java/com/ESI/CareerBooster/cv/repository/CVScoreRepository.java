package com.ESI.CareerBooster.cv.repository;

import com.ESI.CareerBooster.cv.model.CVScore;
import com.ESI.CareerBooster.cv.model.CV;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CVScoreRepository extends JpaRepository<CVScore, Long> {
    
    /**
     * Find CV score by CV
     */
    Optional<CVScore> findByCv(CV cv);
    
    /**
     * Find CV score by CV ID
     */
    Optional<CVScore> findByCvId(Long cvId);
    
    /**
     * Find all CV scores for a user (through CV relationship)
     */
    @Query("SELECT cs FROM CVScore cs WHERE cs.cv.user.email = :userEmail ORDER BY cs.createdAt DESC")
    List<CVScore> findByUserEmailOrderByCreatedAtDesc(@Param("userEmail") String userEmail);
    
    /**
     * Get average score for a user
     */
    @Query("SELECT AVG(cs.overallScore) FROM CVScore cs WHERE cs.cv.user.email = :userEmail")
    Double getAverageScoreByUserEmail(@Param("userEmail") String userEmail);
    
    /**
     * Get latest score for a user
     */
    @Query("SELECT cs FROM CVScore cs WHERE cs.cv.user.email = :userEmail ORDER BY cs.createdAt DESC LIMIT 1")
    Optional<CVScore> getLatestScoreByUserEmail(@Param("userEmail") String userEmail);
    
    /**
     * Count scores by industry type
     */
    long countByIndustryType(String industryType);
    
    /**
     * Find scores by industry type
     */
    List<CVScore> findByIndustryTypeOrderByOverallScoreDesc(String industryType);
    
    /**
     * Check if CV has been scored
     */
    boolean existsByCv(CV cv);
}
