package com.saasproject.modules.audit.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Audit log entry stored in MongoDB.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_logs")
public class AuditLog {

    @Id
    private String id;

    @Indexed
    private String tenantId;

    @Indexed
    private String userId;

    private String username;

    @Indexed
    private String action;

    @Indexed
    private String entityType;

    private String entityId;

    private Map<String, Object> oldValue;

    private Map<String, Object> newValue;

    private String ipAddress;

    private String userAgent;

    private String requestUri;

    private String requestMethod;

    private Integer responseStatus;

    private Long durationMs;

    @Indexed
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private Map<String, Object> metadata;
}
