package com.java_web_service_qlkh_cdda.config;

import com.java_web_service_qlkh_cdda.entity.User;
import com.java_web_service_qlkh_cdda.enums.RoleEnum;
import com.java_web_service_qlkh_cdda.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Seed default Admin
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .email("admin@qlkh.edu.vn")
                    .fullName("System Administrator")
                    .role(RoleEnum.ADMIN)
                    .isActive(true)
                    .build();
            userRepository.save(admin);
            log.info("[INIT] Default admin created — username: admin / password: Admin@123");
        }

        // Seed default Lecturer
        if (!userRepository.existsByUsername("lecturer01")) {
            User lecturer = User.builder()
                    .username("lecturer01")
                    .passwordHash(passwordEncoder.encode("Lecturer@123"))
                    .email("lecturer01@qlkh.edu.vn")
                    .fullName("Nguyễn Văn Giảng")
                    .role(RoleEnum.LECTURER)
                    .isActive(true)
                    .build();
            userRepository.save(lecturer);
            log.info("[INIT] Default lecturer created — username: lecturer01 / password: Lecturer@123");
        }

        // Seed default Student
        if (!userRepository.existsByUsername("student01")) {
            User student = User.builder()
                    .username("student01")
                    .passwordHash(passwordEncoder.encode("Student@123"))
                    .email("student01@qlkh.edu.vn")
                    .fullName("Trần Thị Sinh Viên")
                    .role(RoleEnum.STUDENT)
                    .isActive(true)
                    .build();
            userRepository.save(student);
            log.info("[INIT] Default student created — username: student01 / password: Student@123");
        }
    }
}