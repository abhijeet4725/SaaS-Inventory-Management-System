package com.saasproject.modules.supplier.repository;

import com.saasproject.modules.supplier.entity.Supplier;
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
 * Supplier repository.
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    @Query("SELECT s FROM Supplier s WHERE s.tenantId = :tenantId AND s.deleted = false ORDER BY s.name")
    Page<Supplier> findByTenant(@Param("tenantId") String tenantId, Pageable pageable);

    @Query("SELECT s FROM Supplier s WHERE s.id = :id AND s.tenantId = :tenantId AND s.deleted = false")
    Optional<Supplier> findByIdAndTenant(@Param("id") UUID id, @Param("tenantId") String tenantId);

    @Query("SELECT s FROM Supplier s WHERE s.tenantId = :tenantId AND s.active = true AND s.deleted = false ORDER BY s.name")
    List<Supplier> findActiveByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT s FROM Supplier s WHERE s.tenantId = :tenantId AND s.deleted = false " +
            "AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(s.supplierCode) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Supplier> search(@Param("tenantId") String tenantId, @Param("query") String query, Pageable pageable);

    boolean existsBySupplierCodeAndTenantIdAndDeletedFalse(String supplierCode, String tenantId);
}
