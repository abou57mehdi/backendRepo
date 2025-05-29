package com.ESI.CareerBooster.cv.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseRecommendation {
    private String title;
    private String description;
    private String url;
    private String platform;
    private String level;
} 