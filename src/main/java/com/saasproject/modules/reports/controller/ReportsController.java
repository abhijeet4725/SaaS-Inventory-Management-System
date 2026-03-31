package com.saasproject.modules.reports.controller;

import com.saasproject.common.api_response.ApiResponse;
import com.saasproject.modules.reports.dto.ReportDto;
import com.saasproject.modules.reports.service.ReportsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Reports controller for dashboard and analytics.
 */
@RestController
@RequestMapping("/v1/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@Tag(name = "Reports", description = "Dashboard and analytics reports")
public class ReportsController {

    private final ReportsService reportsService;

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard summary", description = "Get key metrics for dashboard")
    public ResponseEntity<ApiResponse<ReportDto.DashboardSummary>> getDashboardSummary() {

        ReportDto.DashboardSummary summary = reportsService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/sales")
    @Operation(summary = "Sales report", description = "Get sales report for date range")
    public ResponseEntity<ApiResponse<ReportDto.SalesReport>> getSalesReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        ReportDto.SalesReport report = reportsService.getSalesReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/inventory")
    @Operation(summary = "Inventory report", description = "Get inventory valuation report")
    public ResponseEntity<ApiResponse<ReportDto.InventoryReport>> getInventoryReport() {

        ReportDto.InventoryReport report = reportsService.getInventoryReport();
        return ResponseEntity.ok(ApiResponse.success(report));
    }
}
