package com.java_web_service_qlkh_cdda.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java_web_service_qlkh_cdda.security.CustomUserDetailsService;
import com.java_web_service_qlkh_cdda.service.TokenBlacklistService;
import com.java_web_service_qlkh_cdda.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Filter — mỗi request chạy qua 1 lần.
 *
 * Thứ tự kiểm tra:
 *   1. Có Authorization header không?
 *   2. Token có trong Redis blacklist không? → 401 ngay lập tức (O(1), ~0.1ms)
 *   3. Parse username từ JWT
 *   4. Token còn hiệu lực không?
 *   5. Set SecurityContext
 *
 * ✅ Bước 2 dùng Redis → không query MySQL → không bottleneck
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService; // ✅ Redis
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            // ─── BƯỚC 1: Kiểm tra Redis blacklist ─────────────────────────────
            // O(1) ~0.1ms — nhanh hơn MySQL query ~10-100 lần
            if (tokenBlacklistService.isBlacklisted(jwt)) {
                log.warn("[JWT-FILTER] Blacklisted token — IP: {} URI: {}",
                        request.getRemoteAddr(), request.getRequestURI());
                writeJsonError(response, 401, "Token has been revoked. Please login again.");
                return;
            }

            // ─── BƯỚC 2: Parse và validate JWT ────────────────────────────────
            final String username = jwtUtil.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (!userDetails.isEnabled()) {
                    writeJsonError(response, 403, "Account is disabled.");
                    return;
                }

                if (jwtUtil.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("[JWT-FILTER] Authenticated '{}' → {}",
                            username, userDetails.getAuthorities());
                }
            }

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("[JWT-FILTER] Expired JWT for URI: {}", request.getRequestURI());
        } catch (io.jsonwebtoken.JwtException e) {
            log.warn("[JWT-FILTER] Malformed JWT: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[JWT-FILTER] Unexpected error: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private void writeJsonError(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status);
        body.put("error", status == 401 ? "Unauthorized" : "Forbidden");
        body.put("message", message);

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}