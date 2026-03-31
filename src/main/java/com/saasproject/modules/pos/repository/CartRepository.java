package com.saasproject.modules.pos.repository;

import com.saasproject.modules.pos.entity.Cart;
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
 * Cart repository.
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    @Query("SELECT c FROM Cart c WHERE c.id = :id AND c.tenantId = :tenantId AND c.deleted = false")
    Optional<Cart> findByIdAndTenant(@Param("id") UUID id, @Param("tenantId") String tenantId);

    @Query("SELECT c FROM Cart c WHERE c.tenantId = :tenantId AND c.status = :status AND c.deleted = false ORDER BY c.createdAt DESC")
    List<Cart> findByTenantAndStatus(@Param("tenantId") String tenantId, @Param("status") Cart.CartStatus status);

    @Query("SELECT c FROM Cart c WHERE c.tenantId = :tenantId AND c.deleted = false ORDER BY c.createdAt DESC")
    Page<Cart> findByTenant(@Param("tenantId") String tenantId, Pageable pageable);
}
