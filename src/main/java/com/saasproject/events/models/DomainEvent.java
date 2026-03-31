package com.saasproject.events.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base domain event class.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class DomainEvent {

    private UUID eventId;
    private String eventType;
    private String tenantId;
    private String userId;
    private LocalDateTime timestamp;

    protected DomainEvent(String eventType, String tenantId, String userId) {
        this.eventId = UUID.randomUUID();
        this.eventType = eventType;
        this.tenantId = tenantId;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
    }
}
