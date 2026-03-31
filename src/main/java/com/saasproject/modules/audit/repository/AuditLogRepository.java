package com.saasproject.modules.audit.repository;

import com.saasproject.modules.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * MongoDB repository for audit logs.
 */
@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    Page<AuditLog> findByTenantIdOrderByTimestampDesc(String tenantId, Pageable pageable);

    Page<AuditLog> findByTenantIdAndUserIdOrderByTimestampDesc(
            String tenantId, String userId, Pageable pageable);

    Page<AuditLog> findByTenantIdAndEntityTypeOrderByTimestampDesc(
            String tenantId, String entityType, Pageable pageable);

    Page<AuditLog> findByTenantIdAndEntityTypeAndEntityIdOrderByTimestampDesc(
            String tenantId, String entityType, String entityId, Pageable pageable);

    Page<AuditLog> findByTenantIdAndTimestampBetweenOrderByTimestampDesc(
            String tenantId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<AuditLog> findByTenantIdAndActionOrderByTimestampDesc(
            String tenantId, String action, Pageable pageable);

    long countByTenantId(String tenantId);
}
