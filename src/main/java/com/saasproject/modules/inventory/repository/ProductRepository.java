package com.saasproject.modules.inventory.repository;

import com.saasproject.modules.inventory.entity.Product;
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
 * Product repository with tenant-aware queries.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

        @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND p.deleted = false")
        Page<Product> findByTenant(@Param("tenantId") String tenantId, Pageable pageable);

        @Query("SELECT p FROM Product p WHERE p.id = :id AND p.tenantId = :tenantId AND p.deleted = false")
        Optional<Product> findByIdAndTenant(@Param("id") UUID id, @Param("tenantId") String tenantId);

        @Query("SELECT p FROM Product p WHERE p.barcode = :barcode AND p.tenantId = :tenantId AND p.deleted = false")
        Optional<Product> findByBarcodeAndTenant(@Param("barcode") String barcode, @Param("tenantId") String tenantId);

        @Query("SELECT p FROM Product p WHERE p.sku = :sku AND p.tenantId = :tenantId AND p.deleted = false")
        Optional<Product> findBySkuAndTenant(@Param("sku") String sku, @Param("tenantId") String tenantId);

        @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND p.deleted = false " +
                        "AND p.trackInventory = true AND p.currentStock <= p.minStockLevel")
        List<Product> findLowStockProducts(@Param("tenantId") String tenantId);

        @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND p.deleted = false " +
                        "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
                        "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) " +
                        "OR p.barcode LIKE CONCAT('%', :query, '%'))")
        Page<Product> search(@Param("tenantId") String tenantId, @Param("query") String query, Pageable pageable);

        @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND p.category = :category AND p.deleted = false")
        Page<Product> findByCategory(@Param("tenantId") String tenantId, @Param("category") String category,
                        Pageable pageable);

        boolean existsBySkuAndTenantIdAndDeletedFalse(String sku, String tenantId);

        boolean existsByBarcodeAndTenantIdAndDeletedFalse(String barcode, String tenantId);

        @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND p.active = true AND p.deleted = false ORDER BY p.name")
        java.util.List<Product> findActiveByTenant(@Param("tenantId") String tenantId);
}
