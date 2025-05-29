package com.ESI.CareerBooster.cvGenerator.service;

import com.ESI.CareerBooster.cvGenerator.model.CVGenerator;
import java.util.List;
import java.util.Optional;

public interface CVGeneratorService {
    
    /**
     * Create a new CV for a user
     */
    CVGenerator createCV(CVGenerator cv, String userId);
    
    /**
     * Get all CVs for a specific user
     */
    List<CVGenerator> getAllCVsByUserId(String userId);
    
    /**
     * Get a specific CV by ID (with user validation)
     */
    Optional<CVGenerator> getCVById(Long id, String userId);
    
    /**
     * Update an existing CV
     */
    CVGenerator updateCV(Long id, CVGenerator updatedCV, String userId);
    
    /**
     * Delete a CV
     */
    boolean deleteCV(Long id, String userId);
    
    /**
     * Generate PDF for a CV
     */
    byte[] generateCVAsPdf(Long id, String userId, String template);
    
    /**
     * Get CV count for a user
     */
    long getCVCountByUserId(String userId);
    
    /**
     * Check if CV exists and belongs to user
     */
    boolean existsCVForUser(Long id, String userId);
    
    /**
     * Get CVs by template type
     */
    List<CVGenerator> getCVsByTemplate(String userId, String template);
}
