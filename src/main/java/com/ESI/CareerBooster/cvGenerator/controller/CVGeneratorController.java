package com.ESI.CareerBooster.cvGenerator.controller;

import com.ESI.CareerBooster.cvGenerator.dto.CVGeneratorDTO;
import com.ESI.CareerBooster.cvGenerator.model.CVGenerator;
import com.ESI.CareerBooster.cvGenerator.service.CVGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cv-generator")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:8081", "http://localhost:3000"})
public class CVGeneratorController {

    private final CVGeneratorService cvGeneratorService;

    @PostMapping
    public ResponseEntity<CVGeneratorDTO> createCV(
            @RequestBody CVGeneratorDTO cvDTO, 
            Authentication authentication) {
        
        try {
            String userEmail = authentication.getName();
            log.info("Creating CV for user: {}", userEmail);
            
            CVGenerator cv = cvDTO.toEntity();
            CVGenerator createdCV = cvGeneratorService.createCV(cv, userEmail);
            
            return new ResponseEntity<>(CVGeneratorDTO.fromEntity(createdCV), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating CV: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<CVGeneratorDTO>> getAllCVs(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            log.info("Fetching all CVs for user: {}", userEmail);
            
            List<CVGenerator> cvs = cvGeneratorService.getAllCVsByUserId(userEmail);
            List<CVGeneratorDTO> cvDTOs = cvs.stream()
                    .map(CVGeneratorDTO::fromEntity)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(cvDTOs);
        } catch (Exception e) {
            log.error("Error fetching CVs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CVGeneratorDTO> getCVById(
            @PathVariable Long id, 
            Authentication authentication) {
        
        try {
            String userEmail = authentication.getName();
            log.info("Fetching CV with ID: {} for user: {}", id, userEmail);
            
            Optional<CVGenerator> cvOpt = cvGeneratorService.getCVById(id, userEmail);
            
            if (cvOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(CVGeneratorDTO.fromEntity(cvOpt.get()));
        } catch (Exception e) {
            log.error("Error fetching CV: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CVGeneratorDTO> updateCV(
            @PathVariable Long id, 
            @RequestBody CVGeneratorDTO cvDTO,
            Authentication authentication) {
        
        try {
            String userEmail = authentication.getName();
            log.info("Updating CV with ID: {} for user: {}", id, userEmail);
            
            CVGenerator cv = cvDTO.toEntity();
            CVGenerator updatedCV = cvGeneratorService.updateCV(id, cv, userEmail);
            
            return ResponseEntity.ok(CVGeneratorDTO.fromEntity(updatedCV));
        } catch (RuntimeException e) {
            log.warn("CV not found or access denied: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating CV: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCV(
            @PathVariable Long id, 
            Authentication authentication) {
        
        try {
            String userEmail = authentication.getName();
            log.info("Deleting CV with ID: {} for user: {}", id, userEmail);
            
            boolean deleted = cvGeneratorService.deleteCV(id, userEmail);
            
            if (!deleted) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting CV: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/export/pdf")
    public ResponseEntity<byte[]> exportCVAsPdf(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "modern") String template,
            Authentication authentication) {
        
        try {
            String userEmail = authentication.getName();
            log.info("Exporting CV as PDF. ID: {}, Template: {}, User: {}", id, template, userEmail);
            
            byte[] pdfBytes = cvGeneratorService.generateCVAsPdf(id, userEmail, template);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "cv_" + id + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (RuntimeException e) {
            log.warn("CV not found or access denied: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error generating PDF: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> getCVCount(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            long count = cvGeneratorService.getCVCountByUserId(userEmail);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error getting CV count: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
