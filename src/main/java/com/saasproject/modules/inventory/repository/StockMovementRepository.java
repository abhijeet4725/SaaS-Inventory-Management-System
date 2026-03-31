package com.saasproject.modules.inventory.repository;

import com.saasproject.modules.inventory.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stock movement repository.
 */
@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    @Query("SELECT sm FROM StockMovement sm WHERE sm.product.id = :productId AND sm.tenantId = :tenantId ORDER BY sm.createdAt DESC")
    Page<StockMovement> findByProductAndTenant(
            @Param("productId") UUID productId,
            @Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("SELECT sm FROM StockMovement sm WHERE sm.tenantId = :tenantId AND sm.createdAt BETWEEN :start AND :end ORDER BY sm.createdAt DESC")
    Page<StockMovement> findByTenantAndDateRange(
            @Param("tenantId") String tenantId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);
}
