package com.java_web_service_qlkh_cdda.repository;

import com.java_web_service_qlkh_cdda.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByPerformedBy(String performedBy, Pageable pageable);

    Page<AuditLog> findByAction(String action, Pageable pageable);
}