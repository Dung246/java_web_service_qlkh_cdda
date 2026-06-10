package com.java_web_service_qlkh_cdda.controller;

import com.java_web_service_qlkh_cdda.dto.request.CourseRequest;
import com.java_web_service_qlkh_cdda.dto.response.ApiResponse;
import com.java_web_service_qlkh_cdda.dto.response.CourseResponse;
import com.java_web_service_qlkh_cdda.dto.response.PageResponse;
import com.java_web_service_qlkh_cdda.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/courses")
@RequiredArgsConstructor
@Tag(name = "Admin - Course Management", description = "CRUD courses — Admin only")
@SecurityRequirement(name = "bearerAuth")
public class AdminCourseController {

    private final CourseService courseService;

    @GetMapping
    @Operation(summary = "Get all courses with search and pagination")
    public ResponseEntity<ApiResponse<PageResponse<CourseResponse>>> getAllCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getAllCourses(page, size, keyword, isActive)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get course by ID")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getCourseById(id)));
    }

    @PostMapping
    @Operation(summary = "Create new course")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(@Valid @RequestBody CourseRequest request) {
        CourseResponse created = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Course created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update course")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Course updated successfully",
                courseService.updateCourse(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete course")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.success("Course deleted successfully", null));
    }
}