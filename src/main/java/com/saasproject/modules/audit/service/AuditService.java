package com.saasproject.modules.audit.service;

import com.saasproject.common.utils.CommonUtils;
import com.saasproject.modules.audit.entity.AuditLog;
import com.saasproject.modules.audit.repository.AuditLogRepository;
import com.saasproject.tenant.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Audit logging service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Log an audit event asynchronously.
     */
    @Async
    public void logAsync(String action, String entityType, String entityId,
            Map<String, Object> oldValue, Map<String, Object> newValue) {
        try {
            log(action, entityType, entityId, oldValue, newValue, null);
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    /**
     * Log an audit event.
     */
    public void log(String action, String entityType, String entityId,
            Map<String, Object> oldValue, Map<String, Object> newValue,
            Map<String, Object> metadata) {

        String tenantId = TenantContext.getCurrentTenant();
        String userId = null;
        String username = null;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            username = auth.getName();
            userId = username;
        }

        AuditLog.AuditLogBuilder logBuilder = AuditLog.builder()
                .tenantId(tenantId)
                .userId(userId)
                .username(username)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .ipAddress(CommonUtils.getClientIpAddress())
                .metadata(metadata);

        // Add request context if available
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                logBuilder.requestUri(request.getRequestURI());
                logBuilder.requestMethod(request.getMethod());
                logBuilder.userAgent(request.getHeader("User-Agent"));
            }
        } catch (Exception e) {
            // Not in request context
        }

        AuditLog auditLog = logBuilder.build();
        auditLogRepository.save(auditLog);

        log.debug("Audit log created: {} {} {}", action, entityType, entityId);
    }

    /**
     * Get audit logs for tenant.
     */
    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return auditLogRepository.findByTenantIdOrderByTimestampDesc(tenantId, pageable);
    }

    /**
     * Get audit logs for specific entity.
     */
    public Page<AuditLog> getEntityAuditLogs(String entityType, String entityId, Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return auditLogRepository.findByTenantIdAndEntityTypeAndEntityIdOrderByTimestampDesc(
                tenantId, entityType, entityId, pageable);
    }

    /**
     * Get audit logs by date range.
     */
    public Page<AuditLog> getAuditLogsByDateRange(
            LocalDateTime start, LocalDateTime end, Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return auditLogRepository.findByTenantIdAndTimestampBetweenOrderByTimestampDesc(
                tenantId, start, end, pageable);
    }
}
