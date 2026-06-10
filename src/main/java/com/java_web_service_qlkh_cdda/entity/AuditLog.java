package com.java_web_service_qlkh_cdda.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String action;

    @Column(length = 200)
    private String targetEntity;

    @Column
    private Long targetId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(length = 100)
    private String performedBy;

    @Column(length = 50)
    private String ipAddress;

    @Column(length = 10)
    private String httpMethod;

    @Column(length = 300)
    private String requestUri;

    @Column
    private Integer responseStatus;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}