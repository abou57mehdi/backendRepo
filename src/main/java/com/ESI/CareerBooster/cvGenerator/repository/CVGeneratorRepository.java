package com.ESI.CareerBooster.cvGenerator.repository;

import com.ESI.CareerBooster.auth.model.User;
import com.ESI.CareerBooster.cvGenerator.model.CVGenerator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CVGeneratorRepository extends JpaRepository<CVGenerator, Long> {

    /**
     * Find all CVs by user
     */
    List<CVGenerator> findByUser(User user);

    /**
     * Find all CVs by user ordered by creation date (newest first)
     */
    List<CVGenerator> findByUserOrderByCreatedDateDesc(User user);

    /**
     * Find a specific CV by ID and user (for security)
     */
    Optional<CVGenerator> findByIdAndUser(Long id, User user);

    /**
     * Count CVs by user
     */
    long countByUser(User user);

    /**
     * Find CVs by user and template type
     */
    List<CVGenerator> findByUserAndTemplate(User user, String template);

    /**
     * Check if a CV exists for a user
     */
    boolean existsByIdAndUser(Long id, User user);

    /**
     * Delete CV by ID and user (for security)
     */
    void deleteByIdAndUser(Long id, User user);
}
