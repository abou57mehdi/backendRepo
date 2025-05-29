package com.ESI.CareerBooster.cv.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import com.ESI.CareerBooster.cv.dto.CVAnalysisResponse.CourseRecommendation;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CVUploadResponse {
    private String fileName;
    private String recommendations;
    private List<CourseRecommendation> courseRecommendations;
} 