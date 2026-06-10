package com.java_web_service_qlkh_cdda.repository;

import com.java_web_service_qlkh_cdda.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    /**
     * Kiểm tra token có trong blacklist không
     * Dùng @Query thay vì existsByTokenString để tối ưu query
     */
    @Query("SELECT COUNT(t) > 0 FROM TokenBlacklist t WHERE t.tokenString = :token")
    boolean existsByTokenString(@Param("token") String tokenString);

    /**
     * Xoá các token đã hết hạn — chạy định kỳ bởi ScheduledTasks
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM TokenBlacklist t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}