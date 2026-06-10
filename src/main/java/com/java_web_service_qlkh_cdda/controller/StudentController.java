package com.java_web_service_qlkh_cdda.controller;

import com.java_web_service_qlkh_cdda.dto.request.ChangePasswordRequest;
import com.java_web_service_qlkh_cdda.dto.request.EnrollmentRequest;
import com.java_web_service_qlkh_cdda.dto.request.SubmissionRequest;
import com.java_web_service_qlkh_cdda.dto.response.ApiResponse;
import com.java_web_service_qlkh_cdda.dto.response.AssignmentResponse;
import com.java_web_service_qlkh_cdda.dto.response.CourseResponse;
import com.java_web_service_qlkh_cdda.dto.response.EnrollmentResponse;
import com.java_web_service_qlkh_cdda.dto.response.MaterialResponse;
import com.java_web_service_qlkh_cdda.dto.response.PageResponse;
import com.java_web_service_qlkh_cdda.dto.response.SubmissionResponse;
import com.java_web_service_qlkh_cdda.dto.response.UserResponse;
import com.java_web_service_qlkh_cdda.service.AssignmentService;
import com.java_web_service_qlkh_cdda.service.AuthService;
import com.java_web_service_qlkh_cdda.service.CourseService;
import com.java_web_service_qlkh_cdda.service.EnrollmentService;
import com.java_web_service_qlkh_cdda.service.MaterialService;
import com.java_web_service_qlkh_cdda.service.SubmissionService;
import com.java_web_service_qlkh_cdda.service.UserService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
@Tag(name = "Student", description = "Endpoints danh cho Sinh vien")
@SecurityRequirement(name = "bearerAuth")
public class StudentController {

    private final EnrollmentService enrollmentService;
    private final SubmissionService submissionService;
    private final AssignmentService assignmentService;
    private final CourseService courseService;
    private final MaterialService materialService;
    private final UserService userService;
    private final AuthService authService;

    // ─── PROFILE ────────────────────────────────────────────────────────────────

    @GetMapping("/profile")
    @Operation(summary = "Xem thong tin ca nhan")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getCurrentUser(userDetails.getUsername())));
    }

    @PutMapping("/profile/change-password")
    @Operation(summary = "Doi mat khau")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        authService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Mat khau da duoc thay doi thanh cong", null));
    }

    // ─── ENROLLMENT ─────────────────────────────────────────────────────────────

    @PostMapping("/enrollments")
    @Operation(summary = "UC-06: Dang ky tham gia khoa hoc")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enroll(
            @Valid @RequestBody EnrollmentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponse me = userService.getCurrentUser(userDetails.getUsername());
        EnrollmentResponse response = enrollmentService.enrollStudent(me.getId(), request.getCourseId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Dang ky hoc thanh cong", response));
    }

    @DeleteMapping("/enrollments/{courseId}")
    @Operation(summary = "Huy dang ky khoa hoc")
    public ResponseEntity<ApiResponse<Void>> unenroll(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponse me = userService.getCurrentUser(userDetails.getUsername());
        enrollmentService.unenrollStudent(me.getId(), courseId);
        return ResponseEntity.ok(ApiResponse.success("Huy dang ky thanh cong", null));
    }

    @GetMapping("/enrollments")
    @Operation(summary = "Xem danh sach khoa hoc dang hoc")
    public ResponseEntity<ApiResponse<PageResponse<EnrollmentResponse>>> getMyEnrollments(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UserResponse me = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(
                enrollmentService.getEnrollmentsByStudent(me.getId(), page, size)));
    }

    // ─── COURSES ────────────────────────────────────────────────────────────────

    @GetMapping("/courses")
    @Operation(summary = "Xem tat ca khoa hoc dang mo")
    public ResponseEntity<ApiResponse<PageResponse<CourseResponse>>> browseCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.success(
                courseService.getAllCourses(page, size, keyword, true)));
    }

    @GetMapping("/courses/{id}")
    @Operation(summary = "Xem chi tiet khoa hoc")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourse(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getCourseById(id)));
    }

    // ─── ASSIGNMENTS ────────────────────────────────────────────────────────────

    @GetMapping("/assignments/course/{courseId}")
    @Operation(summary = "Xem bai tap cua khoa hoc")
    public ResponseEntity<ApiResponse<PageResponse<AssignmentResponse>>> getAssignments(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                assignmentService.getAssignmentsByCourse(courseId, page, size)));
    }

    @GetMapping("/assignments/{id}")
    @Operation(summary = "Xem chi tiet bai tap")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getAssignment(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(assignmentService.getAssignmentById(id)));
    }

    // ─── SUBMISSIONS ────────────────────────────────────────────────────────────

    @PostMapping("/submissions")
    @Operation(summary = "UC-08: Nop bai tap / Do an (GitHub link)")
    public ResponseEntity<ApiResponse<SubmissionResponse>> submit(
            @Valid @RequestBody SubmissionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        SubmissionResponse response = submissionService.submitAssignment(
                userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Nop bai thanh cong", response));
    }

    @PostMapping(value = "/submissions/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "UC-05: Upload bao cao PDF len cloud storage")
    public ResponseEntity<ApiResponse<SubmissionResponse>> uploadReport(
            @RequestParam Long assignmentId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        SubmissionResponse response = submissionService.uploadReport(
                userDetails.getUsername(), assignmentId, file);
        return ResponseEntity.ok(ApiResponse.success("Upload bao cao thanh cong", response));
    }

    @GetMapping("/submissions")
    @Operation(summary = "Xem tat ca bai nop cua toi")
    public ResponseEntity<ApiResponse<PageResponse<SubmissionResponse>>> getMySubmissions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                submissionService.getMySubmissions(userDetails.getUsername(), page, size)));
    }

    @GetMapping("/submissions/{id}")
    @Operation(summary = "Xem chi tiet bai nop")
    public ResponseEntity<ApiResponse<SubmissionResponse>> getSubmission(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(submissionService.getSubmissionById(id)));
    }

    // ─── MATERIALS ──────────────────────────────────────────────────────────────

    @GetMapping("/materials/course/{courseId}")
    @Operation(summary = "Xem tai lieu bai giang cua khoa hoc")
    public ResponseEntity<ApiResponse<PageResponse<MaterialResponse>>> getMaterials(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                materialService.getMaterialsByCourse(courseId, page, size)));
    }
}