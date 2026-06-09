package com.java_web_service_qlkh_cdda.repository;

import com.java_web_service_qlkh_cdda.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    Page<Enrollment> findByStudentId(Long studentId, Pageable pageable);

    Page<Enrollment> findByCourseId(Long courseId, Pageable pageable);

    long countByCourseId(Long courseId);
}