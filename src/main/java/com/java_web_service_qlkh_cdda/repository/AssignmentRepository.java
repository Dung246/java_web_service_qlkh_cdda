package com.java_web_service_qlkh_cdda.repository;

import com.java_web_service_qlkh_cdda.entity.Assignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    Page<Assignment> findByCourseId(Long courseId, Pageable pageable);

    List<Assignment> findByDeadlineBefore(LocalDateTime deadline);
}