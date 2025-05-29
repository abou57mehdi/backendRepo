package com.ESI.CareerBooster.cv.controller;

import com.ESI.CareerBooster.cv.dto.HomeScreenData;
import com.ESI.CareerBooster.cv.service.HomeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {
    
    private final HomeService homeService;
    
    @GetMapping
    public ResponseEntity<HomeScreenData> getHomeData(@AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Fetching home data for user: {}", userDetails.getUsername());
        try {
            HomeScreenData homeData = homeService.getHomeData(userDetails.getUsername());
            
            // Validate created_at dates
            if (homeData.getUserProgress() != null && 
                homeData.getUserProgress().getLastAnalysisDate() != null) {
                log.debug("Last analysis date: {}", 
                    homeData.getUserProgress().getLastAnalysisDate());
            }
            
            if (homeData.getCvStatus() != null && 
                homeData.getCvStatus().getLastUpdate() != null) {
                log.debug("CV status last update: {}", 
                    homeData.getCvStatus().getLastUpdate());
            }
            
            return ResponseEntity.ok(homeData);
        } catch (Exception e) {
            log.error("Error fetching home data for user {}: {}", 
                userDetails.getUsername(), e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 