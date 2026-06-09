package com.java_web_service_qlkh_cdda.repository;

import com.java_web_service_qlkh_cdda.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCourseCode(String courseCode);

    boolean existsByCourseCode(String courseCode);

    @Query("SELECT c FROM Course c WHERE " +
            "(:keyword IS NULL OR LOWER(c.courseCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.courseName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:isActive IS NULL OR c.isActive = :isActive)")
    Page<Course> searchCourses(@Param("keyword") String keyword,
                               @Param("isActive") Boolean isActive,
                               Pageable pageable);

    Page<Course> findByLecturerId(Long lecturerId, Pageable pageable);
}