package com.saasproject.tenant.interceptor;

import com.saasproject.common.constants.AppConstants;
import com.saasproject.modules.auth.security.JwtTokenProvider;
import com.saasproject.tenant.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interceptor to extract and set the tenant context for each request.
 * 
 * Tenant identification order:
 * 1. JWT token claim (tenantId)
 * 2. X-Tenant-ID header
 * 3. Default tenant from configuration
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.tenant.default-tenant:default}")
    private String defaultTenant;

    @Value("${app.tenant.header-name:X-Tenant-ID}")
    private String tenantHeaderName;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {

        String tenantId = resolveTenantId(request);

        if (tenantId != null) {
            TenantContext.setCurrentTenant(tenantId);
            MDC.put("tenantId", tenantId);
            log.debug("Tenant context set to: {}", tenantId);
        } else {
            log.warn("No tenant ID found, using default: {}", defaultTenant);
            TenantContext.setCurrentTenant(defaultTenant);
            MDC.put("tenantId", defaultTenant);
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler, ModelAndView modelAndView) throws Exception {
        // No action needed
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) throws Exception {
        // Clean up tenant context to prevent memory leaks
        TenantContext.clear();
        MDC.remove("tenantId");
    }

    private String resolveTenantId(HttpServletRequest request) {
        // Try to get tenant from JWT token first
        String authHeader = request.getHeader(AppConstants.AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(AppConstants.BEARER_PREFIX)) {
            String token = authHeader.substring(7);
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    String tenantId = jwtTokenProvider.extractTenantId(token);
                    if (tenantId != null && !tenantId.isBlank()) {
                        return tenantId;
                    }
                }
            } catch (Exception e) {
                log.debug("Could not extract tenant from JWT: {}", e.getMessage());
            }
        }

        // Try X-Tenant-ID header
        String headerTenant = request.getHeader(tenantHeaderName);
        if (headerTenant != null && !headerTenant.isBlank()) {
            return headerTenant;
        }

        return null;
    }
}
