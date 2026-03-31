package com.saasproject.modules.customer.repository;

import com.saasproject.modules.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Customer repository.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    @Query("SELECT c FROM Customer c WHERE c.tenantId = :tenantId AND c.deleted = false ORDER BY c.name")
    Page<Customer> findByTenant(@Param("tenantId") String tenantId, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.id = :id AND c.tenantId = :tenantId AND c.deleted = false")
    Optional<Customer> findByIdAndTenant(@Param("id") UUID id, @Param("tenantId") String tenantId);

    @Query("SELECT c FROM Customer c WHERE c.tenantId = :tenantId AND c.deleted = false " +
            "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR c.phone LIKE CONCAT('%', :query, '%'))")
    Page<Customer> search(@Param("tenantId") String tenantId, @Param("query") String query, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.phone = :phone AND c.tenantId = :tenantId AND c.deleted = false")
    Optional<Customer> findByPhoneAndTenant(@Param("phone") String phone, @Param("tenantId") String tenantId);

    @Query("SELECT c FROM Customer c WHERE c.email = :email AND c.tenantId = :tenantId AND c.deleted = false")
    Optional<Customer> findByEmailAndTenant(@Param("email") String email, @Param("tenantId") String tenantId);

    boolean existsByCustomerCodeAndTenantIdAndDeletedFalse(String customerCode, String tenantId);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.tenantId = :tenantId AND c.createdAt BETWEEN :start AND :end AND c.deleted = false")
    long countByTenantAndCreatedAtBetween(@Param("tenantId") String tenantId,
            @Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);
}
