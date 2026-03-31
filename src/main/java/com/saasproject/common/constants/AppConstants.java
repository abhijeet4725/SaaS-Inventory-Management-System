package com.saasproject.common.constants;

/**
 * Application constants.
 */
public final class AppConstants {

    private AppConstants() {
    }

    // ===== Roles =====
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MANAGER = "MANAGER";
    public static final String ROLE_CASHIER = "CASHIER";
    public static final String ROLE_AUDITOR = "AUDITOR";

    // ===== Prefixes =====
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String INVOICE_PREFIX = "INV";
    public static final String ORDER_PREFIX = "ORD";
    public static final String PURCHASE_PREFIX = "PO";

    // ===== Headers =====
    public static final String TENANT_HEADER = "X-Tenant-ID";
    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    // ===== Pagination =====
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    // ===== Cache Keys =====
    public static final String CACHE_PRODUCTS = "products";
    public static final String CACHE_USERS = "users";
    public static final String CACHE_INVOICES = "invoices";
    public static final String CACHE_TENANTS = "tenants";

    // ===== WebSocket Topics =====
    public static final String WS_TOPIC_POS = "/topic/pos/";
    public static final String WS_TOPIC_INVENTORY = "/topic/inventory/";
    public static final String WS_TOPIC_DASHBOARD = "/topic/dashboard/";
    public static final String WS_TOPIC_NOTIFICATIONS = "/topic/notifications/";

    // ===== Status =====
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";
}
