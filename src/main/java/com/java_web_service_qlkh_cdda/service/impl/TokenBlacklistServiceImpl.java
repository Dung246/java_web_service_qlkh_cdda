package com.java_web_service_qlkh_cdda.service.impl;

import com.java_web_service_qlkh_cdda.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  Redis Token Blacklist — cơ chế hoạt động                              │
 * │                                                                         │
 * │  Khi logout:                                                            │
 * │    SET blacklist:eyJhbGci... "username"  EX 1800   ← accessToken 30p   │
 * │    SET blacklist:eyJhbGci... "username"  EX 604800 ← refreshToken 7d   │
 * │                                                                         │
 * │  Khi request đến:                                                       │
 * │    EXISTS blacklist:eyJhbGci...  → O(1) ~0.1ms                         │
 * │    Nếu = 1 → từ chối ngay, không cần check DB                         │
 * │                                                                         │
 * │  Sau khi hết TTL: Redis tự xóa → bộ nhớ không tăng mãi               │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * Format key Redis: "token_blacklist:{jwt_token}"
 * Value           : "{username}" (dùng để debug, không cần thiết về logic)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    // Prefix tránh đụng key với các feature khác trong Redis
    private static final String BLACKLIST_PREFIX = "token_blacklist:";

    @Override
    public void blacklistToken(String token, String username, long ttlMillis) {
        if (token == null || token.isBlank() || ttlMillis <= 0) return;

        String key = BLACKLIST_PREFIX + token;
        try {
            // SET key value EX ttlSeconds
            // TTL = thời gian còn lại của token → sau đó Redis tự xóa
            redisTemplate.opsForValue().set(
                    key,
                    username != null ? username : "revoked",
                    ttlMillis,
                    TimeUnit.MILLISECONDS
            );
            log.info("[REDIS-BLACKLIST] Token revoked for user '{}' — TTL: {}s",
                    username, ttlMillis / 1000);
        } catch (Exception e) {
            // Redis lỗi không được làm hỏng luồng logout
            log.error("[REDIS-BLACKLIST] Failed to blacklist token: {}", e.getMessage());
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) return false;

        String key = BLACKLIST_PREFIX + token;
        try {
            // O(1) — không scan, không query, chỉ một lệnh GET
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            // Nếu Redis down → fail-open (cho qua) để không block toàn bộ hệ thống
            // Trong production nên dùng Redis Sentinel/Cluster để tránh điều này
            log.error("[REDIS-BLACKLIST] Redis unavailable, fail-open: {}", e.getMessage());
            return false;
        }
    }
}