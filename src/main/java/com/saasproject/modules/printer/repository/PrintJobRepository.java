package com.saasproject.modules.printer.repository;

import com.saasproject.modules.printer.entity.PrintJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for PrintJob entity.
 */
@Repository
public interface PrintJobRepository extends JpaRepository<PrintJob, UUID> {

    @Query("SELECT pj FROM PrintJob pj WHERE pj.tenantId = :tenantId AND pj.deleted = false ORDER BY pj.createdAt DESC")
    Page<PrintJob> findByTenant(@Param("tenantId") String tenantId, Pageable pageable);

    @Query("SELECT pj FROM PrintJob pj WHERE pj.id = :id AND pj.tenantId = :tenantId AND pj.deleted = false")
    Optional<PrintJob> findByIdAndTenant(@Param("id") UUID id, @Param("tenantId") String tenantId);

    @Query("SELECT pj FROM PrintJob pj WHERE pj.tenantId = :tenantId AND pj.status = :status AND pj.deleted = false ORDER BY pj.priority, pj.createdAt")
    List<PrintJob> findByTenantAndStatus(@Param("tenantId") String tenantId,
            @Param("status") PrintJob.JobStatus status);

    @Query("SELECT pj FROM PrintJob pj WHERE pj.status = 'PENDING' AND pj.deleted = false ORDER BY pj.priority, pj.createdAt")
    List<PrintJob> findPendingJobs();

    @Query("SELECT pj FROM PrintJob pj WHERE pj.status = 'FAILED' AND pj.retryCount < 3 AND pj.deleted = false")
    List<PrintJob> findRetryableJobs();

    @Query("SELECT COUNT(pj) FROM PrintJob pj WHERE pj.tenantId = :tenantId AND pj.status = :status AND pj.deleted = false")
    long countByTenantAndStatus(@Param("tenantId") String tenantId, @Param("status") PrintJob.JobStatus status);
}
