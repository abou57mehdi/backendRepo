package com.ESI.CareerBooster.courses;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/courses")
@Tag(name = "Courses", description = "APIs for fetching course recommendations")
public class CourseController {
    @Autowired
    private CourseraService courseraService;

    @Operation(
        summary = "Get courses by category",
        description = "Fetches course recommendations from Coursera based on the specified category"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Courses fetched successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid category parameter"),
        @ApiResponse(responseCode = "500", description = "Error fetching courses from Coursera")
    })
    @GetMapping
    public ResponseEntity<String> getCourses(
        @Parameter(description = "Category of courses to fetch", required = true)
        @RequestParam String category
    ) {
        String result = courseraService.fetchCoursesByCategory(category);
        return ResponseEntity.ok(result);
    }
} 