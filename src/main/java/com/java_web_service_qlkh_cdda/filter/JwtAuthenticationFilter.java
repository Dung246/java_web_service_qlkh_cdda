package com.java_web_service_qlkh_cdda.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java_web_service_qlkh_cdda.repository.TokenBlacklistRepository;
import com.java_web_service_qlkh_cdda.security.CustomUserDetailsService;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Không có token → tiếp tục (Spring Security tự chặn nếu endpoint cần auth)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            // ─── BƯỚC 1: Kiểm tra token có bị blacklist không ─────────────────
            if (tokenBlacklistRepository.existsByTokenString(jwt)) {
                log.warn("[JWT-FILTER] Blacklisted token used — IP: {}, URI: {}",
                        request.getRemoteAddr(), request.getRequestURI());
                writeUnauthorizedResponse(response, "Token has been revoked. Please login again.");
                return;
            }

            // ─── BƯỚC 2: Parse username từ JWT ────────────────────────────────
            final String username = jwtUtil.extractUsername(jwt);

            // ─── BƯỚC 3: Xác thực và set SecurityContext ──────────────────────
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.isTokenValid(jwt, userDetails)) {
                    // Tài khoản có bị khoá không
                    if (!userDetails.isEnabled()) {
                        writeUnauthorizedResponse(response, "Account is disabled.");
                        return;
                    }

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("[JWT-FILTER] Authenticated user '{}' with authorities {}",
                            username, userDetails.getAuthorities());
                } else {
                    log.warn("[JWT-FILTER] Invalid/expired token for user '{}' — URI: {}",
                            username, request.getRequestURI());
                }
            }

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("[JWT-FILTER] Expired JWT: {}", e.getMessage());
            // Không writeResponse ở đây — để Spring Security handler xử lý 401
        } catch (io.jsonwebtoken.JwtException e) {
            log.warn("[JWT-FILTER] Malformed JWT: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[JWT-FILTER] Unexpected error: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Ghi response 401 dạng JSON chuẩn khi token bị revoke
     */
    private void writeUnauthorizedResponse(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", 401);
        body.put("error", "Unauthorized");
        body.put("message", message);

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}