package com.saasproject.modules.printer.repository;

import com.saasproject.modules.printer.entity.Printer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Printer entity.
 */
@Repository
public interface PrinterRepository extends JpaRepository<Printer, UUID> {

    @Query("SELECT p FROM Printer p WHERE p.tenantId = :tenantId AND p.deleted = false ORDER BY p.isDefault DESC, p.name")
    List<Printer> findByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT p FROM Printer p WHERE p.id = :id AND p.tenantId = :tenantId AND p.deleted = false")
    Optional<Printer> findByIdAndTenant(@Param("id") UUID id, @Param("tenantId") String tenantId);

    @Query("SELECT p FROM Printer p WHERE p.tenantId = :tenantId AND p.isDefault = true AND p.deleted = false")
    Optional<Printer> findDefaultByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT p FROM Printer p WHERE p.tenantId = :tenantId AND p.active = true AND p.deleted = false")
    List<Printer> findActiveByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT COUNT(p) FROM Printer p WHERE p.tenantId = :tenantId AND p.deleted = false")
    long countByTenant(@Param("tenantId") String tenantId);
}
