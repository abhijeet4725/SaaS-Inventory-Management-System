package com.saasproject.modules.purchase.repository;

import com.saasproject.modules.purchase.entity.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Purchase Order repository.
 */
@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {

        @Query("SELECT po FROM PurchaseOrder po WHERE po.tenantId = :tenantId AND po.deleted = false ORDER BY po.createdAt DESC")
        Page<PurchaseOrder> findByTenant(@Param("tenantId") String tenantId, Pageable pageable);

        @Query("SELECT po FROM PurchaseOrder po WHERE po.id = :id AND po.tenantId = :tenantId AND po.deleted = false")
        Optional<PurchaseOrder> findByIdAndTenant(@Param("id") UUID id, @Param("tenantId") String tenantId);

        @Query("SELECT po FROM PurchaseOrder po WHERE po.tenantId = :tenantId AND po.status = :status AND po.deleted = false")
        List<PurchaseOrder> findByTenantAndStatus(@Param("tenantId") String tenantId,
                        @Param("status") PurchaseOrder.POStatus status);

        @Query("SELECT po FROM PurchaseOrder po WHERE po.tenantId = :tenantId AND po.supplierId = :supplierId AND po.deleted = false ORDER BY po.createdAt DESC")
        Page<PurchaseOrder> findByTenantAndSupplier(@Param("tenantId") String tenantId,
                        @Param("supplierId") UUID supplierId, Pageable pageable);

        @Query("SELECT po FROM PurchaseOrder po WHERE po.tenantId = :tenantId AND po.deleted = false " +
                        "AND (LOWER(po.poNumber) LIKE LOWER(CONCAT('%', :query, '%')) " +
                        "OR LOWER(po.supplierName) LIKE LOWER(CONCAT('%', :query, '%')))")
        Page<PurchaseOrder> search(@Param("tenantId") String tenantId, @Param("query") String query, Pageable pageable);

        @Query("SELECT COUNT(po) FROM PurchaseOrder po WHERE po.tenantId = :tenantId AND po.status = :status AND po.deleted = false")
        long countByStatus(@Param("tenantId") String tenantId, @Param("status") PurchaseOrder.POStatus status);

        @Query("SELECT po FROM PurchaseOrder po WHERE po.tenantId = :tenantId AND po.status = 'SENT' AND po.expectedDate < :date AND po.deleted = false")
        List<PurchaseOrder> findOverdue(@Param("tenantId") String tenantId, @Param("date") LocalDate date);

        boolean existsByPoNumberAndTenantIdAndDeletedFalse(String poNumber, String tenantId);

        @Query("SELECT SUM(po.totalAmount) FROM PurchaseOrder po WHERE po.tenantId = :tenantId AND po.status = :status AND po.createdAt BETWEEN :start AND :end AND po.deleted = false")
        java.math.BigDecimal sumTotalAmountByStatusAndCreatedAtBetween(
                        @Param("tenantId") String tenantId,
                        @Param("status") PurchaseOrder.POStatus status,
                        @Param("start") java.time.LocalDateTime start,
                        @Param("end") java.time.LocalDateTime end);
}
