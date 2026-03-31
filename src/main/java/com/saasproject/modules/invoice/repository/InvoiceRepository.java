package com.saasproject.modules.invoice.repository;

import com.saasproject.modules.invoice.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Invoice repository.
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

        @Query("SELECT i FROM Invoice i WHERE i.tenantId = :tenantId AND i.deleted = false ORDER BY i.createdAt DESC")
        Page<Invoice> findByTenant(@Param("tenantId") String tenantId, Pageable pageable);

        @Query("SELECT i FROM Invoice i WHERE i.id = :id AND i.tenantId = :tenantId AND i.deleted = false")
        Optional<Invoice> findByIdAndTenant(@Param("id") UUID id, @Param("tenantId") String tenantId);

        Optional<Invoice> findByInvoiceNumberAndTenantIdAndDeletedFalse(String invoiceNumber, String tenantId);

        @Query("SELECT i FROM Invoice i WHERE i.tenantId = :tenantId AND i.status = :status AND i.deleted = false")
        Page<Invoice> findByTenantAndStatus(
                        @Param("tenantId") String tenantId,
                        @Param("status") Invoice.InvoiceStatus status,
                        Pageable pageable);

        @Query("SELECT i FROM Invoice i WHERE i.tenantId = :tenantId AND i.invoiceDate BETWEEN :start AND :end AND i.deleted = false")
        Page<Invoice> findByTenantAndDateRange(
                        @Param("tenantId") String tenantId,
                        @Param("start") LocalDate start,
                        @Param("end") LocalDate end,
                        Pageable pageable);

        @Query("SELECT COUNT(i) FROM Invoice i WHERE i.tenantId = :tenantId AND i.deleted = false")
        long countByTenant(@Param("tenantId") String tenantId);

        @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(i.invoiceNumber, 13) AS int)), 0) FROM Invoice i WHERE i.tenantId = :tenantId")
        int getLastInvoiceNumber(@Param("tenantId") String tenantId);

        @Query("SELECT new com.saasproject.modules.reports.dto.ReportDto$TopProduct(" +
                        "it.productId, it.productName, SUM(it.quantity), SUM(it.amount)) " +
                        "FROM Invoice i JOIN i.items it " +
                        "WHERE i.tenantId = :tenantId AND i.status = 'PAID' AND i.deleted = false " +
                        "GROUP BY it.productId, it.productName " +
                        "ORDER BY SUM(it.amount) DESC")
        java.util.List<com.saasproject.modules.reports.dto.ReportDto.TopProduct> findTopSellingProducts(
                        @Param("tenantId") String tenantId, Pageable pageable);

        @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.tenantId = :tenantId AND i.status = :status AND i.deleted = false")
        java.math.BigDecimal sumTotalAmountByStatus(@Param("tenantId") String tenantId,
                        @Param("status") Invoice.InvoiceStatus status);

        @Query("SELECT i FROM Invoice i WHERE i.tenantId = :tenantId AND i.status = :status AND i.deleted = false ORDER BY i.createdAt DESC")
        java.util.List<Invoice> findByTenantAndStatus(@Param("tenantId") String tenantId,
                        @Param("status") Invoice.InvoiceStatus status);

        @Query("SELECT i FROM Invoice i WHERE i.tenantId = :tenantId AND i.invoiceDate BETWEEN :start AND :end AND i.deleted = false ORDER BY i.invoiceDate DESC")
        java.util.List<Invoice> findByTenantAndDateRange(@Param("tenantId") String tenantId,
                        @Param("start") java.time.LocalDate start, @Param("end") java.time.LocalDate end);
}
