package com.java_web_service_qlkh_cdda.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_blacklist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * JWT token dài ~400-600 ký tự
     * utf8mb4 = 4 bytes/char → 700 * 4 = 2800 bytes < giới hạn MySQL InnoDB 3072 bytes
     * KHÔNG dùng VARCHAR(1000) vì 1000*4=4000 > 3072 → lỗi "key too long"
     */
    @Column(nullable = false, unique = true, length = 700)
    private String tokenString;

    @Column(nullable = false)
    private LocalDateTime revokedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}