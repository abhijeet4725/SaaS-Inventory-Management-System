package com.saasproject.modules.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Report DTOs for dashboard and analytics.
 */
public class ReportDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardSummary {
        private SalesMetrics sales;
        private InventoryMetrics inventory;
        private CustomerMetrics customers;
        private PurchaseMetrics purchases;
        private List<TopProduct> topProducts;
        private List<RecentSale> recentSales;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesMetrics {
        private BigDecimal todaySales;
        private BigDecimal weekSales;
        private BigDecimal monthSales;
        private BigDecimal yearSales;
        private int todayTransactions;
        private int monthTransactions;
        private BigDecimal averageOrderValue;
        private BigDecimal growthPercent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryMetrics {
        private int totalProducts;
        private int lowStockCount;
        private int outOfStockCount;
        private BigDecimal inventoryValue;
        private int categoriesCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerMetrics {
        private int totalCustomers;
        private int newCustomersThisMonth;
        private int activeCustomers;
        private BigDecimal totalRevenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseMetrics {
        private int pendingOrders;
        private int overdueOrders;
        private BigDecimal pendingValue;
        private BigDecimal monthPurchases;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProduct {
        private String productId;
        private String productName;
        private String productSku;
        private int quantitySold;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentSale {
        private String invoiceId;
        private String invoiceNumber;
        private String customerName;
        private BigDecimal amount;
        private String date;
        private String paymentMethod;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesReport {
        private String period;
        private String startDate;
        private String endDate;
        private BigDecimal totalSales;
        private BigDecimal totalTax;
        private BigDecimal totalDiscount;
        private BigDecimal netSales;
        private int transactionCount;
        private BigDecimal averageOrderValue;
        private Map<String, BigDecimal> salesByPaymentMethod;
        private List<DailySales> dailyBreakdown;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailySales {
        private String date;
        private BigDecimal sales;
        private int transactions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryReport {
        private int totalProducts;
        private BigDecimal totalValue;
        private int lowStockItems;
        private int outOfStockItems;
        private List<ProductStock> products;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductStock {
        private String productId;
        private String productName;
        private String sku;
        private String category;
        private int currentStock;
        private int reorderLevel;
        private BigDecimal costPrice;
        private BigDecimal sellingPrice;
        private BigDecimal stockValue;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfitReport {
        private String period;
        private BigDecimal revenue;
        private BigDecimal costOfGoods;
        private BigDecimal grossProfit;
        private BigDecimal grossMarginPercent;
        private List<CategoryProfit> byCategory;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryProfit {
        private String category;
        private BigDecimal revenue;
        private BigDecimal cost;
        private BigDecimal profit;
        private BigDecimal marginPercent;
    }
}
