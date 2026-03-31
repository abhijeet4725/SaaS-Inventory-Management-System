package com.saasproject.events.listeners;

import com.saasproject.events.models.InventoryEvents;
import com.saasproject.modules.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Inventory event listeners.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventListener {

    private final AuditService auditService;
    private final SimpMessagingTemplate messagingTemplate;

    @Async
    @EventListener
    public void handleProductCreated(InventoryEvents.ProductCreatedEvent event) {
        log.info("Product created: {} - {}", event.getProductId(), event.getProductName());

        // Create audit log
        auditService.log(
                "PRODUCT_CREATED",
                "Product",
                event.getProductId().toString(),
                null,
                Map.of(
                        "name", event.getProductName(),
                        "sku", event.getSku() != null ? event.getSku() : ""),
                null);

        // Broadcast via WebSocket
        messagingTemplate.convertAndSend(
                "/topic/inventory/" + event.getTenantId(),
                Map.of(
                        "type", "PRODUCT_CREATED",
                        "productId", event.getProductId(),
                        "productName", event.getProductName()));
    }

    @Async
    @EventListener
    public void handleStockUpdated(InventoryEvents.StockUpdatedEvent event) {
        log.info("Stock updated for product: {} - {} -> {}",
                event.getProductName(), event.getPreviousStock(), event.getNewStock());

        // Create audit log
        auditService.log(
                "STOCK_UPDATED",
                "Product",
                event.getProductId().toString(),
                Map.of("stock", event.getPreviousStock()),
                Map.of("stock", event.getNewStock(), "change", event.getChange(), "reason", event.getReason()),
                null);

        // Broadcast via WebSocket
        messagingTemplate.convertAndSend(
                "/topic/inventory/" + event.getTenantId(),
                Map.of(
                        "type", "STOCK_UPDATED",
                        "productId", event.getProductId(),
                        "productName", event.getProductName(),
                        "currentStock", event.getNewStock(),
                        "change", event.getChange()));
    }

    @Async
    @EventListener
    public void handleLowStockAlert(InventoryEvents.LowStockAlertEvent event) {
        log.warn("Low stock alert: {} - Current: {}, Min: {}",
                event.getProductName(), event.getCurrentStock(), event.getMinStockLevel());

        // Broadcast alert via WebSocket
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + event.getTenantId(),
                Map.of(
                        "type", "LOW_STOCK_ALERT",
                        "productId", event.getProductId(),
                        "productName", event.getProductName(),
                        "currentStock", event.getCurrentStock(),
                        "minStockLevel", event.getMinStockLevel(),
                        "severity", "WARNING"));
    }
}
