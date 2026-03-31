package com.saasproject.common.exceptions;

import lombok.Getter;

/**
 * Exception thrown when a requested resource is not found.
 */
@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final String resourceId;

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s with ID '%s' not found", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public ResourceNotFoundException(String resourceType, String field, String value) {
        super(String.format("%s with %s '%s' not found", resourceType, field, value));
        this.resourceType = resourceType;
        this.resourceId = value;
    }
}
