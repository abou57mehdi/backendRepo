package com.ESI.CareerBooster.cv.controller;

import com.ESI.CareerBooster.cv.model.CVScore;
import com.ESI.CareerBooster.cv.repository.CVScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cv-scores")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "CV Score", description = "CV scoring and analysis endpoints")
public class CVScoreController {

    private final CVScoreRepository cvScoreRepository;

    @GetMapping
    @Operation(summary = "Get all CV scores for authenticated user")
    public ResponseEntity<List<CVScore>> getUserCVScores(@AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Fetching CV scores for user: {}", userDetails.getUsername());

        List<CVScore> scores = cvScoreRepository.findByUserEmailOrderByCreatedAtDesc(userDetails.getUsername());
        return ResponseEntity.ok(scores);
    }

    @GetMapping("/latest")
    @Operation(summary = "Get latest CV score for authenticated user")
    public ResponseEntity<CVScore> getLatestCVScore(@AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Fetching latest CV score for user: {}", userDetails.getUsername());

        Optional<CVScore> latestScore = cvScoreRepository.getLatestScoreByUserEmail(userDetails.getUsername());

        if (latestScore.isPresent()) {
            return ResponseEntity.ok(latestScore.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/average")
    @Operation(summary = "Get average CV score for authenticated user")
    public ResponseEntity<Double> getAverageCVScore(@AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Fetching average CV score for user: {}", userDetails.getUsername());

        Double averageScore = cvScoreRepository.getAverageScoreByUserEmail(userDetails.getUsername());

        if (averageScore != null) {
            return ResponseEntity.ok(averageScore);
        } else {
            return ResponseEntity.ok(0.0);
        }
    }

    @GetMapping("/{cvId}")
    @Operation(summary = "Get CV score by CV ID")
    public ResponseEntity<CVScore> getCVScore(@PathVariable Long cvId, @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Fetching CV score for CV ID: {} and user: {}", cvId, userDetails.getUsername());

        Optional<CVScore> score = cvScoreRepository.findByCvId(cvId);

        if (score.isPresent()) {
            // Verify the CV belongs to the authenticated user
            if (score.get().getCv().getUser().getEmail().equals(userDetails.getUsername())) {
                return ResponseEntity.ok(score.get());
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/industry/{industryType}")
    @Operation(summary = "Get CV scores by industry type for authenticated user")
    public ResponseEntity<List<CVScore>> getCVScoresByIndustry(
            @PathVariable String industryType,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Fetching CV scores for industry: {} and user: {}", industryType, userDetails.getUsername());

        List<CVScore> scores = cvScoreRepository.findByUserEmailOrderByCreatedAtDesc(userDetails.getUsername())
                .stream()
                .filter(score -> industryType.equalsIgnoreCase(score.getIndustryType()))
                .toList();

        return ResponseEntity.ok(scores);
    }
}
