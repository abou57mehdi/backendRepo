package com.ESI.CareerBooster.cv.repository;

import com.ESI.CareerBooster.cv.model.CV;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CVRepository extends JpaRepository<CV, Long> {
    List<CV> findByUserId(Long userId);
    List<CV> findByUserEmailOrderByCreatedAtDesc(String email);
} 