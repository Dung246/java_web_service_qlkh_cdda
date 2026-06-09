package com.java_web_service_qlkh_cdda.repository;

import com.java_web_service_qlkh_cdda.entity.Submission;
import com.java_web_service_qlkh_cdda.enums.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    Optional<Submission> findByStudentIdAndAssignmentId(Long studentId, Long assignmentId);

    boolean existsByStudentIdAndAssignmentId(Long studentId, Long assignmentId);

    Page<Submission> findByStudentId(Long studentId, Pageable pageable);

    Page<Submission> findByAssignmentId(Long assignmentId, Pageable pageable);

    Page<Submission> findByCourseId(Long courseId, Pageable pageable);

    Page<Submission> findByStatus(StatusEnum status, Pageable pageable);

    @Query("SELECT s FROM Submission s WHERE s.assignment.id = :assignmentId AND s.status = :status")
    Page<Submission> findByAssignmentIdAndStatus(@Param("assignmentId") Long assignmentId,
                                                 @Param("status") StatusEnum status,
                                                 Pageable pageable);

    @Query("SELECT AVG(s.score) FROM Submission s WHERE s.course.id = :courseId AND s.status = 'GRADED'")
    Double findAverageScoreByCourseId(@Param("courseId") Long courseId);

    List<Submission> findByStatus(StatusEnum status);
}