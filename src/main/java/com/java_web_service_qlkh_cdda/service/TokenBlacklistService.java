package com.java_web_service_qlkh_cdda.service;

/**
 * Interface trừu tượng hoá việc blacklist token.
 * Implementation dùng Redis thay vì MySQL.
 *
 * WHY REDIS thay vì DB:
 * ─────────────────────────────────────────────────────────────────────────────
 * Vấn đề với MySQL:
 *   • Mỗi request vào hệ thống đều phải query DB: SELECT COUNT(*) FROM token_blacklist
 *     WHERE token_string = ?  →  nếu có 10.000 req/s = 10.000 DB queries/s chỉ để
 *     check blacklist → tắc nghẽn cổ chai (bottleneck) nghiêm trọng.
 *   • Bảng token_blacklist ngày càng phình to → slow query.
 *   • Cần scheduled job để dọn dẹp token hết hạn.
 *
 * Giải pháp Redis:
 *   • Redis là in-memory store → latency ~0.1ms (MySQL disk I/O ~1-10ms).
 *   • Tự động xóa key khi hết hạn qua TTL (EXPIRE) → không cần scheduled job.
 *   • Throughput Redis đạt 100.000+ ops/s trên single node.
 *   • Hoàn toàn không ảnh hưởng MySQL khi check blacklist.
 * ─────────────────────────────────────────────────────────────────────────────
 */
public interface TokenBlacklistService {

    /**
     * Thêm token vào blacklist với TTL = thời gian còn lại đến khi token hết hạn.
     * Redis tự động xóa sau khi TTL hết → không tốn bộ nhớ.
     *
     * @param token     chuỗi JWT (access hoặc refresh)
     * @param username  dùng làm value để debug trong Redis CLI
     * @param ttlMillis thời gian sống (ms) — bằng thời gian hết hạn còn lại của token
     */
    void blacklistToken(String token, String username, long ttlMillis);

    /**
     * Kiểm tra token có bị blacklist không.
     * Redis GET O(1) — cực nhanh, không lock, không I/O disk.
     *
     * @param token chuỗi JWT cần kiểm tra
     * @return true nếu đã bị blacklist
     */
    boolean isBlacklisted(String token);
}