package com.saasproject.common.exceptions;

/**
 * Exception thrown when a user tries to access resources from another tenant.
 */
public class TenantAccessException extends RuntimeException {

    public TenantAccessException(String message) {
        super(message);
    }

    public TenantAccessException() {
        super("Access denied: You cannot access resources from another tenant");
    }
}
