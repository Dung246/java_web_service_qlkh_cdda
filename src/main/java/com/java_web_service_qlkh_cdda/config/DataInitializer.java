package com.java_web_service_qlkh_cdda.config;

import com.java_web_service_qlkh_cdda.entity.Assignment;
import com.java_web_service_qlkh_cdda.entity.Course;
import com.java_web_service_qlkh_cdda.entity.Enrollment;
import com.java_web_service_qlkh_cdda.entity.Material;
import com.java_web_service_qlkh_cdda.entity.Submission;
import com.java_web_service_qlkh_cdda.entity.User;
import com.java_web_service_qlkh_cdda.enums.RoleEnum;
import com.java_web_service_qlkh_cdda.enums.StatusEnum;
import com.java_web_service_qlkh_cdda.repository.AssignmentRepository;
import com.java_web_service_qlkh_cdda.repository.CourseRepository;
import com.java_web_service_qlkh_cdda.repository.EnrollmentRepository;
import com.java_web_service_qlkh_cdda.repository.MaterialRepository;
import com.java_web_service_qlkh_cdda.repository.SubmissionRepository;
import com.java_web_service_qlkh_cdda.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository       userRepository;
    private final CourseRepository     courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final MaterialRepository   materialRepository;
    private final PasswordEncoder      passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // Chỉ seed khi DB trống hoàn toàn
        if (userRepository.count() > 0) {
            log.info("[INIT] Data đã tồn tại — bỏ qua seed");
            return;
        }

        log.info("[INIT] ========== BẮT ĐẦU SEED DỮ LIỆU MẪU ==========");

        List<User> lecturers = seedUsers();
        List<Course> courses = seedCourses(lecturers);
        List<User> students  = getStudents();
        List<Assignment> assignments = seedAssignments(courses, lecturers);
        seedEnrollments(students, courses);
        seedSubmissions(students, assignments, courses);
        seedMaterials(courses, lecturers);

        log.info("[INIT] ========== SEED HOÀN TẤT ==========");
        log.info("[INIT] Tài khoản:");
        log.info("[INIT]   admin      / Admin@123    (ADMIN)");
        log.info("[INIT]   lecturer01 / Lecturer@123 (LECTURER)");
        log.info("[INIT]   lecturer02 / Lecturer@123 (LECTURER)");
        log.info("[INIT]   lecturer03 / Lecturer@123 (LECTURER)");
        log.info("[INIT]   lecturer04 / Lecturer@123 (LECTURER)");
        log.info("[INIT]   lecturer05 / Lecturer@123 (LECTURER)");
        log.info("[INIT]   student01  / Student@123  (STUDENT)");
        log.info("[INIT]   student02  / Student@123  (STUDENT)");
        log.info("[INIT]   student03  / Student@123  (STUDENT)");
        log.info("[INIT]   student04  / Student@123  (STUDENT)");
        log.info("[INIT]   student05  / Student@123  (STUDENT)");
    }

    // ─── USERS ────────────────────────────────────────────────────────────────
    private List<User> seedUsers() {
        // 1 Admin
        User admin = userRepository.save(User.builder()
                .username("admin")
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .email("admin@qlkh.edu.vn")
                .fullName("Nguyễn Quản Trị")
                .role(RoleEnum.ADMIN)
                .isActive(true)
                .build());

        // 5 Giảng viên
        User l1 = userRepository.save(User.builder()
                .username("lecturer01")
                .passwordHash(passwordEncoder.encode("Lecturer@123"))
                .email("lecturer01@qlkh.edu.vn")
                .fullName("Trần Văn An")
                .role(RoleEnum.LECTURER).isActive(true).build());

        User l2 = userRepository.save(User.builder()
                .username("lecturer02")
                .passwordHash(passwordEncoder.encode("Lecturer@123"))
                .email("lecturer02@qlkh.edu.vn")
                .fullName("Lê Thị Bình")
                .role(RoleEnum.LECTURER).isActive(true).build());

        User l3 = userRepository.save(User.builder()
                .username("lecturer03")
                .passwordHash(passwordEncoder.encode("Lecturer@123"))
                .email("lecturer03@qlkh.edu.vn")
                .fullName("Phạm Minh Cường")
                .role(RoleEnum.LECTURER).isActive(true).build());

        User l4 = userRepository.save(User.builder()
                .username("lecturer04")
                .passwordHash(passwordEncoder.encode("Lecturer@123"))
                .email("lecturer04@qlkh.edu.vn")
                .fullName("Hoàng Thị Dung")
                .role(RoleEnum.LECTURER).isActive(true).build());

        User l5 = userRepository.save(User.builder()
                .username("lecturer05")
                .passwordHash(passwordEncoder.encode("Lecturer@123"))
                .email("lecturer05@qlkh.edu.vn")
                .fullName("Vũ Quốc Đạt")
                .role(RoleEnum.LECTURER).isActive(true).build());

        // 5 Sinh viên
        userRepository.save(User.builder()
                .username("student01")
                .passwordHash(passwordEncoder.encode("Student@123"))
                .email("student01@qlkh.edu.vn")
                .fullName("Nguyễn Văn Hùng")
                .role(RoleEnum.STUDENT).isActive(true).build());

        userRepository.save(User.builder()
                .username("student02")
                .passwordHash(passwordEncoder.encode("Student@123"))
                .email("student02@qlkh.edu.vn")
                .fullName("Trần Thị Lan")
                .role(RoleEnum.STUDENT).isActive(true).build());

        userRepository.save(User.builder()
                .username("student03")
                .passwordHash(passwordEncoder.encode("Student@123"))
                .email("student03@qlkh.edu.vn")
                .fullName("Lê Minh Khoa")
                .role(RoleEnum.STUDENT).isActive(true).build());

        userRepository.save(User.builder()
                .username("student04")
                .passwordHash(passwordEncoder.encode("Student@123"))
                .email("student04@qlkh.edu.vn")
                .fullName("Phạm Thị Mai")
                .role(RoleEnum.STUDENT).isActive(true).build());

        userRepository.save(User.builder()
                .username("student05")
                .passwordHash(passwordEncoder.encode("Student@123"))
                .email("student05@qlkh.edu.vn")
                .fullName("Hoàng Văn Nam")
                .role(RoleEnum.STUDENT).isActive(true).build());

        log.info("[INIT]  Users: 1 admin + 5 lecturers + 5 students");
        return List.of(l1, l2, l3, l4, l5);
    }

    // ─── COURSES ──────────────────────────────────────────────────────────────
    private List<Course> seedCourses(List<User> lecturers) {
        Course c1 = courseRepository.save(Course.builder()
                .courseCode("JAVA101")
                .courseName("Lập trình Java Web Service")
                .description("Xây dựng REST API với Spring Boot 3, Spring Security, JWT, AOP và JPA")
                .credit(3)
                .lecturer(lecturers.get(0))
                .isActive(true).build());

        Course c2 = courseRepository.save(Course.builder()
                .courseCode("DB201")
                .courseName("Cơ sở dữ liệu nâng cao")
                .description("SQL nâng cao, stored procedures, indexing, query optimization, MySQL")
                .credit(3)
                .lecturer(lecturers.get(1))
                .isActive(true).build());

        Course c3 = courseRepository.save(Course.builder()
                .courseCode("NET301")
                .courseName("Lập trình mạng máy tính")
                .description("TCP/IP, Socket programming, HTTP protocol, RESTful architecture")
                .credit(3)
                .lecturer(lecturers.get(2))
                .isActive(true).build());

        Course c4 = courseRepository.save(Course.builder()
                .courseCode("AI401")
                .courseName("Trí tuệ nhân tạo cơ bản")
                .description("Machine learning, deep learning, Python, scikit-learn, TensorFlow")
                .credit(4)
                .lecturer(lecturers.get(3))
                .isActive(true).build());

        Course c5 = courseRepository.save(Course.builder()
                .courseCode("SE501")
                .courseName("Kỹ nghệ phần mềm")
                .description("Quy trình phát triển phần mềm, Agile, Scrum, UML, design patterns")
                .credit(3)
                .lecturer(lecturers.get(4))
                .isActive(true).build());

        log.info("[INIT] Courses: 5 môn học");
        return List.of(c1, c2, c3, c4, c5);
    }

    // ─── ASSIGNMENTS ──────────────────────────────────────────────────────────
    private List<Assignment> seedAssignments(List<Course> courses, List<User> lecturers) {
        Assignment a1 = assignmentRepository.save(Assignment.builder()
                .title("Đồ án cuối kỳ: Xây dựng REST API với Spring Boot")
                .description("Sinh viên xây dựng hệ thống backend hoàn chỉnh: " +
                        "Spring Boot 3, Spring Security + JWT, JPA, AOP Logging. " +
                        "Nộp link GitHub + báo cáo PDF.")
                .deadline(LocalDateTime.of(2026, 12, 31, 23, 59, 59))
                .maxScore(100.0)
                .course(courses.get(0))
                .createdBy(lecturers.get(0))
                .build());

        Assignment a2 = assignmentRepository.save(Assignment.builder()
                .title("Bài tập thực hành: Thiết kế Database ERD")
                .description("Thiết kế cơ sở dữ liệu cho hệ thống quản lý thư viện. " +
                        "Vẽ ERD, normalize đến 3NF, viết script SQL tạo bảng.")
                .deadline(LocalDateTime.of(2026, 11, 30, 23, 59, 59))
                .maxScore(10.0)
                .course(courses.get(1))
                .createdBy(lecturers.get(1))
                .build());

        Assignment a3 = assignmentRepository.save(Assignment.builder()
                .title("Lab 3: Lập trình Socket Client-Server")
                .description("Xây dựng ứng dụng chat đơn giản dùng Java Socket. " +
                        "Server hỗ trợ nhiều client đồng thời (multi-thread).")
                .deadline(LocalDateTime.of(2026, 10, 15, 23, 59, 59))
                .maxScore(10.0)
                .course(courses.get(2))
                .createdBy(lecturers.get(2))
                .build());

        Assignment a4 = assignmentRepository.save(Assignment.builder()
                .title("Project: Xây dựng mô hình phân loại ảnh")
                .description("Dùng TensorFlow/Keras xây dựng CNN phân loại ảnh CIFAR-10. " +
                        "Đạt accuracy >= 80% trên tập test. Nộp notebook + báo cáo.")
                .deadline(LocalDateTime.of(2026, 12, 15, 23, 59, 59))
                .maxScore(100.0)
                .course(courses.get(3))
                .createdBy(lecturers.get(3))
                .build());

        Assignment a5 = assignmentRepository.save(Assignment.builder()
                .title("Tiểu luận: Phân tích quy trình Scrum trong dự án thực tế")
                .description("Phân tích một dự án phần mềm thực tế áp dụng Scrum. " +
                        "Nêu ưu nhược điểm, bài học kinh nghiệm. Tối thiểu 10 trang.")
                .deadline(LocalDateTime.of(2026, 11, 15, 23, 59, 59))
                .maxScore(10.0)
                .course(courses.get(4))
                .createdBy(lecturers.get(4))
                .build());

        log.info("[INIT]  Assignments: 5 bài tập");
        return List.of(a1, a2, a3, a4, a5);
    }

    // ─── ENROLLMENTS ──────────────────────────────────────────────────────────
    private void seedEnrollments(List<User> students, List<Course> courses) {
        // Mỗi sinh viên đăng ký 1 môn khác nhau (+ student01 đăng ký thêm 1 môn)
        save(students.get(0), courses.get(0)); // student01 → JAVA101
        save(students.get(0), courses.get(1)); // student01 → DB201
        save(students.get(1), courses.get(1)); // student02 → DB201
        save(students.get(2), courses.get(2)); // student03 → NET301
        save(students.get(3), courses.get(3)); // student04 → AI401
        save(students.get(4), courses.get(4)); // student05 → SE501

        log.info("[INIT]  Enrollments: 6 đăng ký");
    }

    private void save(User student, Course course) {
        if (!enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            enrollmentRepository.save(Enrollment.builder()
                    .student(student)
                    .course(course)
                    .isActive(true)
                    .build());
        }
    }

    // ─── SUBMISSIONS ──────────────────────────────────────────────────────────
    private void seedSubmissions(List<User> students, List<Assignment> assignments, List<Course> courses) {
        // submission 1 — SUBMITTED (chưa chấm)
        submissionRepository.save(Submission.builder()
                .student(students.get(0))
                .assignment(assignments.get(0))
                .course(courses.get(0))
                .githubUrl("https://github.com/nguyenvanhung/java-webservice-final")
                .status(StatusEnum.SUBMITTED)
                .build());

        // submission 2 — GRADED (đã chấm điểm)
        submissionRepository.save(Submission.builder()
                .student(students.get(0))
                .assignment(assignments.get(1))
                .course(courses.get(1))
                .githubUrl("https://github.com/nguyenvanhung/db-erd-lab")
                .reportUrl("https://res.cloudinary.com/demo/raw/upload/v1/reports/sv01_db_report.pdf")
                .score(8.5)
                .feedback("ERD thiết kế tốt, đúng 3NF. Cần bổ sung thêm index cho các cột thường query.")
                .status(StatusEnum.GRADED)
                .gradedBy(students.get(0)) // placeholder — sẽ là lecturer trong thực tế
                .gradedAt(LocalDateTime.now().minusDays(2))
                .build());

        // submission 3 — LATE (nộp trễ)
        submissionRepository.save(Submission.builder()
                .student(students.get(1))
                .assignment(assignments.get(1))
                .course(courses.get(1))
                .githubUrl("https://github.com/tranthilan/db-assignment")
                .status(StatusEnum.LATE)
                .build());

        // submission 4 — SUBMITTED (có file báo cáo)
        submissionRepository.save(Submission.builder()
                .student(students.get(2))
                .assignment(assignments.get(2))
                .course(courses.get(2))
                .githubUrl("https://github.com/leminhkhoa/socket-chat")
                .reportUrl("https://res.cloudinary.com/demo/raw/upload/v1/reports/sv03_net_report.pdf")
                .status(StatusEnum.SUBMITTED)
                .build());

        // submission 5 — PENDING (chưa nộp — chỉ tạo record)
        submissionRepository.save(Submission.builder()
                .student(students.get(3))
                .assignment(assignments.get(3))
                .course(courses.get(3))
                .status(StatusEnum.PENDING)
                .build());

        log.info("[INIT]  Submissions: 5 bài nộp (SUBMITTED/GRADED/LATE/PENDING)");
    }

    // ─── MATERIALS ────────────────────────────────────────────────────────────
    private void seedMaterials(List<Course> courses, List<User> lecturers) {
        materialRepository.save(Material.builder()
                .title("Slide Tuần 1: Giới thiệu Spring Boot & RESTful API")
                .description("Tổng quan kiến trúc Spring Boot, auto-configuration, annotation cơ bản")
                .fileUrl("https://res.cloudinary.com/demo/raw/upload/v1/materials/java101_week1.pdf")
                .fileType("application/pdf")
                .fileSize(2_048_000L)
                .course(courses.get(0))
                .uploadedBy(lecturers.get(0))
                .build());

        materialRepository.save(Material.builder()
                .title("Slide Tuần 2: Spring Security & JWT Authentication")
                .description("Cấu hình Spring Security, JWT filter, phân quyền theo role")
                .fileUrl("https://res.cloudinary.com/demo/raw/upload/v1/materials/java101_week2.pdf")
                .fileType("application/pdf")
                .fileSize(3_145_728L)
                .course(courses.get(0))
                .uploadedBy(lecturers.get(0))
                .build());

        materialRepository.save(Material.builder()
                .title("Tài liệu: MySQL Performance Tuning")
                .description("Kỹ thuật tối ưu query, indexing strategy, explain plan")
                .fileUrl("https://res.cloudinary.com/demo/raw/upload/v1/materials/db201_perf.pdf")
                .fileType("application/pdf")
                .fileSize(1_572_864L)
                .course(courses.get(1))
                .uploadedBy(lecturers.get(1))
                .build());

        materialRepository.save(Material.builder()
                .title("Lab Guide: Socket Programming với Java")
                .description("Hướng dẫn từng bước lập trình Socket, ServerSocket, multi-thread server")
                .fileUrl("https://res.cloudinary.com/demo/raw/upload/v1/materials/net301_socket.pdf")
                .fileType("application/pdf")
                .fileSize(1_048_576L)
                .course(courses.get(2))
                .uploadedBy(lecturers.get(2))
                .build());

        materialRepository.save(Material.builder()
                .title("Notebook mẫu: CNN với TensorFlow/Keras")
                .description("Jupyter notebook hướng dẫn xây dựng mạng CNN, train và evaluate model")
                .fileUrl("https://res.cloudinary.com/demo/raw/upload/v1/materials/ai401_cnn.pdf")
                .fileType("application/pdf")
                .fileSize(4_194_304L)
                .course(courses.get(3))
                .uploadedBy(lecturers.get(3))
                .build());

        log.info("[INIT] Materials: 5 tài liệu bài giảng");
    }

    // Helper lấy danh sách students từ DB
    private List<User> getStudents() {
        return userRepository.findByRole(RoleEnum.STUDENT,
                        org.springframework.data.domain.PageRequest.of(0, 10))
                .getContent();
    }
}