package com.java_web_service_qlkh_cdda.entity;

/**
 * ⚠️  DEPRECATED — Không còn dùng nữa.
 *
 * Token blacklist đã được chuyển sang Redis để tránh bottleneck:
 *   - Trước: query MySQL mỗi request → chậm khi traffic cao
 *   - Sau  : Redis GET O(1) ~0.1ms  → không ảnh hưởng DB
 *
 * File này giữ lại để không break code cũ, nhưng class đã bị xóa Entity annotation.
 * Hibernate sẽ không tạo bảng token_blacklist nữa.
 *
 * @see com.java_web_service_qlkh_cdda.service.TokenBlacklistService
 * @see com.java_web_service_qlkh_cdda.service.impl.TokenBlacklistServiceImpl
 */
public class TokenBlacklist {
    // Intentionally empty — replaced by Redis
}