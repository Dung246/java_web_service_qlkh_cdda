package com.java_web_service_qlkh_cdda.controller;

import com.java_web_service_qlkh_cdda.dto.request.AssignmentRequest;
import com.java_web_service_qlkh_cdda.dto.request.GradeRequest;
import com.java_web_service_qlkh_cdda.dto.response.*;
import com.java_web_service_qlkh_cdda.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/lecturer")
@RequiredArgsConstructor
@Tag(name = "Lecturer", description = "Endpoints for Lecturers")
@SecurityRequirement(name = "bearerAuth")
public class LecturerController {

    private final GradeService gradeService;
    private final AssignmentService assignmentService;
    private final MaterialService materialService;
    private final SubmissionService submissionService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final UserService userService;

    // ─── GRADING ───────────────────────────────────────────────────────────────

    @PostMapping("/grades")
    @Operation(summary = "UC-04: Grade a submission (triggers AOP @AfterReturning log)")
    public ResponseEntity<ApiResponse<SubmissionResponse>> gradeSubmission(
            @Valid @RequestBody GradeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        SubmissionResponse response = gradeService.gradeSubmission(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Graded successfully", response));
    }

    @PatchMapping("/grades/{submissionId}/return")
    @Operation(summary = "Return submission to student for re-submission")
    public ResponseEntity<ApiResponse<SubmissionResponse>> returnSubmission(
            @PathVariable Long submissionId,
            @RequestParam(required = false) String feedback,
            @AuthenticationPrincipal UserDetails userDetails) {
        SubmissionResponse response = gradeService.returnSubmission(submissionId, feedback, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Submission returned to student", response));
    }

    // ─── ASSIGNMENTS ────────────────────────────────────────────────────────────

    @PostMapping("/assignments")
    @Operation(summary = "Create a new assignment for a course")
    public ResponseEntity<ApiResponse<AssignmentResponse>> createAssignment(
            @Valid @RequestBody AssignmentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        AssignmentResponse response = assignmentService.createAssignment(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Assignment created successfully", response));
    }

    @GetMapping("/assignments/{id}")
    @Operation(summary = "Get assignment by ID")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getAssignment(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(assignmentService.getAssignmentById(id)));
    }

    @PutMapping("/assignments/{id}")
    @Operation(summary = "Update assignment")
    public ResponseEntity<ApiResponse<AssignmentResponse>> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Assignment updated",
                assignmentService.updateAssignment(id, request)));
    }

    @DeleteMapping("/assignments/{id}")
    @Operation(summary = "Delete assignment")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.ok(ApiResponse.success("Assignment deleted", null));
    }

    @GetMapping("/assignments/course/{courseId}")
    @Operation(summary = "Get all assignments for a course")
    public ResponseEntity<ApiResponse<PageResponse<AssignmentResponse>>> getAssignmentsByCourse(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                assignmentService.getAssignmentsByCourse(courseId, page, size)));
    }

    // ─── SUBMISSIONS VIEW ───────────────────────────────────────────────────────

    @GetMapping("/submissions/assignment/{assignmentId}")
    @Operation(summary = "Get all submissions for an assignment")
    public ResponseEntity<ApiResponse<PageResponse<SubmissionResponse>>> getSubmissionsByAssignment(
            @PathVariable Long assignmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                submissionService.getSubmissionsByAssignment(assignmentId, page, size)));
    }

    @GetMapping("/submissions/course/{courseId}")
    @Operation(summary = "Get all submissions for a course")
    public ResponseEntity<ApiResponse<PageResponse<SubmissionResponse>>> getSubmissionsByCourse(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                submissionService.getSubmissionsByCourse(courseId, page, size)));
    }

    @GetMapping("/submissions/{id}")
    @Operation(summary = "Get submission by ID")
    public ResponseEntity<ApiResponse<SubmissionResponse>> getSubmissionById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(submissionService.getSubmissionById(id)));
    }

    // ─── MATERIALS ──────────────────────────────────────────────────────────────

    @PostMapping(value = "/materials/course/{courseId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "UC-09: Upload lecture material to cloud storage")
    public ResponseEntity<ApiResponse<MaterialResponse>> uploadMaterial(
            @PathVariable Long courseId,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        MaterialResponse response = materialService.uploadMaterial(
                courseId, title, description, file, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Material uploaded successfully", response));
    }

    @GetMapping("/materials/course/{courseId}")
    @Operation(summary = "Get all materials for a course")
    public ResponseEntity<ApiResponse<PageResponse<MaterialResponse>>> getMaterialsByCourse(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                materialService.getMaterialsByCourse(courseId, page, size)));
    }

    @DeleteMapping("/materials/{id}")
    @Operation(summary = "Delete material")
    public ResponseEntity<ApiResponse<Void>> deleteMaterial(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        materialService.deleteMaterial(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Material deleted", null));
    }

    // ─── COURSES & ENROLLMENTS VIEW ─────────────────────────────────────────────

    @GetMapping("/courses")
    @Operation(summary = "Get courses assigned to current lecturer")
    public ResponseEntity<ApiResponse<PageResponse<CourseResponse>>> getMyCourses(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UserResponse me = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(
                courseService.getCoursesByLecturer(me.getId(), page, size)));
    }

    @GetMapping("/enrollments/course/{courseId}")
    @Operation(summary = "Get all students enrolled in a course")
    public ResponseEntity<ApiResponse<PageResponse<EnrollmentResponse>>> getEnrollmentsByCourse(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                enrollmentService.getEnrollmentsByCourse(courseId, page, size)));
    }

    // ─── PROFILE ────────────────────────────────────────────────────────────────

    @GetMapping("/profile")
    @Operation(summary = "Get current lecturer profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(userService.getCurrentUser(userDetails.getUsername())));
    }
}