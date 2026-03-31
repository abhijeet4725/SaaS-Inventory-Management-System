package com.saasproject.tenant.context;

import lombok.extern.slf4j.Slf4j;

/**
 * Thread-local storage for the current tenant ID.
 * 
 * The tenant context is set by the TenantInterceptor and can be
 * accessed throughout the request lifecycle.
 */
@Slf4j
public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    /**
     * Set the current tenant ID for this thread.
     */
    public static void setCurrentTenant(String tenantId) {
        log.debug("Setting tenant context: {}", tenantId);
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * Get the current tenant ID for this thread.
     */
    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    /**
     * Clear the tenant context. Should be called at the end of each request.
     */
    public static void clear() {
        log.debug("Clearing tenant context");
        CURRENT_TENANT.remove();
    }

    /**
     * Check if a tenant context is set.
     */
    public static boolean hasTenant() {
        return CURRENT_TENANT.get() != null;
    }
}
