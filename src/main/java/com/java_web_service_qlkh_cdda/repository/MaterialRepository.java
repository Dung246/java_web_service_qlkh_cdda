package com.java_web_service_qlkh_cdda.repository;

import com.java_web_service_qlkh_cdda.entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    Page<Material> findByCourseId(Long courseId, Pageable pageable);

    Page<Material> findByUploadedById(Long userId, Pageable pageable);
}