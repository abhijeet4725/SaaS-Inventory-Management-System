package com.saasproject.modules.reports.service;

import com.saasproject.modules.customer.repository.CustomerRepository;
import com.saasproject.modules.inventory.entity.Product;
import com.saasproject.modules.inventory.repository.ProductRepository;
import com.saasproject.modules.invoice.entity.Invoice;
import com.saasproject.modules.invoice.repository.InvoiceRepository;
import com.saasproject.modules.purchase.repository.PurchaseOrderRepository;
import com.saasproject.modules.reports.dto.ReportDto;
import com.saasproject.tenant.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportsServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @InjectMocks
    private ReportsService reportsService;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant("test-tenant");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void getDashboardSummary_ShouldReturnMetrics() {
        // Mock Sales Metrics
        when(invoiceRepository.findByTenantAndStatus("test-tenant", Invoice.InvoiceStatus.PAID))
                .thenReturn(Collections.emptyList());

        // Mock Inventory Metrics
        when(productRepository.findActiveByTenant("test-tenant"))
                .thenReturn(Collections.emptyList());

        // Mock Customer Metrics
        when(customerRepository.count()).thenReturn(10L);
        when(customerRepository.countByTenantAndCreatedAtBetween(any(), any(), any())).thenReturn(2L);
        when(invoiceRepository.sumTotalAmountByStatus("test-tenant", Invoice.InvoiceStatus.PAID))
                .thenReturn(BigDecimal.valueOf(1000));

        // Mock Purchase Metrics
        when(purchaseOrderRepository.countByStatus(any(), any())).thenReturn(5L);
        when(purchaseOrderRepository.findOverdue(any(), any())).thenReturn(Collections.emptyList());
        when(purchaseOrderRepository.sumTotalAmountByStatusAndCreatedAtBetween(any(), any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(500));

        // Execute
        ReportDto.DashboardSummary summary = reportsService.getDashboardSummary();

        // Verify
        assertNotNull(summary);
        assertEquals(10, summary.getCustomers().getTotalCustomers());
        assertEquals(2, summary.getCustomers().getNewCustomersThisMonth());
        assertEquals(BigDecimal.valueOf(1000), summary.getCustomers().getTotalRevenue());
    }

    @Test
    void getCustomerMetrics_ShouldCalculateRevenue() {
        // Setup
        when(customerRepository.count()).thenReturn(50L);
        when(invoiceRepository.sumTotalAmountByStatus("test-tenant", Invoice.InvoiceStatus.PAID))
                .thenReturn(BigDecimal.valueOf(50000.50));

        // Execute
        ReportDto.DashboardSummary summary = reportsService.getDashboardSummary(); // internally calls
                                                                                   // getCustomerMetrics

        // Verify
        assertEquals(BigDecimal.valueOf(50000.50), summary.getCustomers().getTotalRevenue());
    }
}
