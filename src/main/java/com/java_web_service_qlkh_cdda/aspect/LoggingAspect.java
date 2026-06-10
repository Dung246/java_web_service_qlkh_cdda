package com.java_web_service_qlkh_cdda.aspect;

import com.java_web_service_qlkh_cdda.entity.AuditLog;
import com.java_web_service_qlkh_cdda.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class LoggingAspect {

    private final AuditLogRepository auditLogRepository;

    // ─── Log all Service layer method execution time ───────────────────────────

    @Around("execution(* com.java_web_service_qlkh_cdda.service..*(..))")
    public Object logServiceExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();

        try {
            Object result = joinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.debug("[SERVICE] {} executed successfully in {}ms", methodName, elapsedTime);
            return result;
        } catch (Throwable ex) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.error("[SERVICE] {} failed after {}ms — Error: {}", methodName, elapsedTime, ex.getMessage());
            throw ex;
        }
    }

    // ─── Log after grading a submission ────────────────────────────────────────

    @AfterReturning(
            pointcut = "execution(* com.java_web_service_qlkh_cdda.service.impl.GradeServiceImpl.gradeSubmission(..))",
            returning = "result"
    )
    public void logAfterGrading(JoinPoint joinPoint, Object result) {
        Object[] args = joinPoint.getArgs();
        String username = getCurrentUsername();

        if (args.length > 0) {
            log.info("[GRADING] Lecturer '{}' graded submission. Args: {}", username, args[0]);
        }

        saveAuditLog("GRADE_SUBMISSION", "Submission", null,
                String.format("Lecturer '%s' graded a submission with result: %s", username, result),
                username);
    }

    // ─── Log after throwing exception in grading ───────────────────────────────

    @AfterThrowing(
            pointcut = "execution(* com.java_web_service_qlkh_cdda.service.impl.GradeServiceImpl.*(..))",
            throwing = "ex"
    )
    public void logGradingException(JoinPoint joinPoint, Throwable ex) {
        String username = getCurrentUsername();
        log.error("[GRADING-ERROR] Method '{}' threw exception: {} — User: {}",
                joinPoint.getSignature().getName(), ex.getMessage(), username);
        saveAuditLog("GRADE_ERROR", "Submission", null,
                String.format("Error in grading by '%s': %s", username, ex.getMessage()),
                username);
    }

    // ─── Log after user login ───────────────────────────────────────────────────

    @AfterReturning(
            pointcut = "execution(* com.java_web_service_qlkh_cdda.service.impl.AuthServiceImpl.login(..))",
            returning = "result"
    )
    public void logAfterLogin(JoinPoint joinPoint, Object result) {
        Object[] args = joinPoint.getArgs();
        String username = args.length > 0 ? args[0].toString() : "unknown";
        log.info("[AUTH] User '{}' logged in successfully", username);
        saveAuditLog("LOGIN", "User", null,
                String.format("User '%s' logged in", username), username);
    }

    // ─── Log after user logout ──────────────────────────────────────────────────

    @AfterReturning(
            pointcut = "execution(* com.java_web_service_qlkh_cdda.service.impl.AuthServiceImpl.logout(..))"
    )
    public void logAfterLogout(JoinPoint joinPoint) {
        String username = getCurrentUsername();
        log.info("[AUTH] User '{}' logged out", username);
        saveAuditLog("LOGOUT", "User", null,
                String.format("User '%s' logged out", username), username);
    }

    // ─── Log after file upload ──────────────────────────────────────────────────

    @AfterReturning(
            pointcut = "execution(* com.java_web_service_qlkh_cdda.service.impl.CloudinaryServiceImpl.uploadFile(..))",
            returning = "url"
    )
    public void logAfterFileUpload(JoinPoint joinPoint, Object url) {
        String username = getCurrentUsername();
        log.info("[UPLOAD] User '{}' uploaded file → URL: {}", username, url);
        saveAuditLog("FILE_UPLOAD", "File", null,
                String.format("User '%s' uploaded file, URL: %s", username, url), username);
    }

    // ─── Log every REST controller request ─────────────────────────────────────

    @Before("execution(* com.java_web_service_qlkh_cdda.controller..*(..))")
    public void logControllerRequest(JoinPoint joinPoint) {
        String username = getCurrentUsername();
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            log.info("[REQUEST] {} {} — by '{}' — Method: {}",
                    request.getMethod(), request.getRequestURI(),
                    username, joinPoint.getSignature().toShortString());
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attrs.getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    private void saveAuditLog(String action, String entity, Long entityId,
                              String details, String performedBy) {
        try {
            HttpServletRequest request = getCurrentRequest();
            AuditLog log = AuditLog.builder()
                    .action(action)
                    .targetEntity(entity)
                    .targetId(entityId)
                    .details(details)
                    .performedBy(performedBy)
                    .ipAddress(request != null ? request.getRemoteAddr() : "unknown")
                    .httpMethod(request != null ? request.getMethod() : "UNKNOWN")
                    .requestUri(request != null ? request.getRequestURI() : "unknown")
                    .createdAt(LocalDateTime.now())
                    .build();
            auditLogRepository.save(log);
        } catch (Exception e) {
            // AOP must never break business logic
        }
    }
}