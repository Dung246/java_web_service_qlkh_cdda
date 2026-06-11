package com.java_web_service_qlkh_cdda.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisHealthConfig {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Kiểm tra kết nối Redis khi app khởi động.
     * Nếu Redis không có → cảnh báo nhưng app vẫn chạy (fail-open).
     */
    @EventListener(ApplicationReadyEvent.class)
    public void checkRedisConnection() {
        try {
            String pong = redisTemplate.getConnectionFactory()
                    .getConnection().ping();
            log.info("Redis connected — PING: {}", pong);
            log.info("Token blacklist: sử dụng Redis (không query MySQL)");
        } catch (Exception e) {
            log.warn("⚠️  Redis không kết nối được: {}", e.getMessage());
            log.warn("⚠️  Token blacklist sẽ fail-open — chạy được nhưng logout không an toàn");
            log.warn("⚠️  Khởi động Redis: redis-server hoặc docker run -p 6379:6379 redis");
        }
    }
}