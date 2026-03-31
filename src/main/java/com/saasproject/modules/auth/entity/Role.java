package com.saasproject.modules.auth.entity;

/**
 * User roles for authorization.
 */
public enum Role {
    /**
     * Full system access - manages users, settings, subscriptions
     */
    ADMIN,

    /**
     * Store/branch manager - manages inventory, reports, staff
     */
    MANAGER,

    /**
     * Point of sale operator - processes sales, handles checkout
     */
    CASHIER,

    /**
     * Read-only access - views reports and audit logs
     */
    AUDITOR
}
