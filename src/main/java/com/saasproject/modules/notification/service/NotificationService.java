package com.saasproject.modules.notification.service;

import com.saasproject.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Notification service for real-time and persistent notifications.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send real-time notification via WebSocket.
     */
    public void sendRealTimeNotification(String tenantId, String type, String title, String message,
            Map<String, Object> data) {
        log.info("Sending real-time notification to tenant: {} - {}", tenantId, title);

        Map<String, Object> notification = Map.of(
                "id", UUID.randomUUID().toString(),
                "type", type,
                "title", title,
                "message", message,
                "data", data != null ? data : Map.of(),
                "timestamp", LocalDateTime.now().toString(),
                "read", false);

        messagingTemplate.convertAndSend("/topic/notifications/" + tenantId, notification);
    }

    /**
     * Send notification to specific user.
     */
    public void sendToUser(String userId, String type, String title, String message) {
        log.info("Sending notification to user: {} - {}", userId, title);

        Map<String, Object> notification = Map.of(
                "id", UUID.randomUUID().toString(),
                "type", type,
                "title", title,
                "message", message,
                "timestamp", LocalDateTime.now().toString(),
                "read", false);

        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notification);
    }

    /**
     * Broadcast to all users in tenant.
     */
    public void broadcast(String title, String message) {
        String tenantId = TenantContext.getCurrentTenant();
        sendRealTimeNotification(tenantId, "BROADCAST", title, message, null);
    }

    // ===== Specific Notification Types =====

    public void notifyNewSale(String tenantId, String invoiceNumber, String amount) {
        sendRealTimeNotification(tenantId, "NEW_SALE", "New Sale",
                "New sale: " + invoiceNumber + " - " + amount,
                Map.of("invoiceNumber", invoiceNumber, "amount", amount));
    }

    public void notifyLowStock(String tenantId, String productName, int currentStock) {
        sendRealTimeNotification(tenantId, "LOW_STOCK", "Low Stock Alert",
                productName + " is running low (" + currentStock + " remaining)",
                Map.of("productName", productName, "currentStock", currentStock));
    }

    public void notifyPOApproved(String tenantId, String poNumber) {
        sendRealTimeNotification(tenantId, "PO_APPROVED", "PO Approved",
                "Purchase Order " + poNumber + " has been approved",
                Map.of("poNumber", poNumber));
    }

    public void notifyPOReceived(String tenantId, String poNumber) {
        sendRealTimeNotification(tenantId, "PO_RECEIVED", "Items Received",
                "Items from PO " + poNumber + " have been received",
                Map.of("poNumber", poNumber));
    }

    public void notifyNewCustomer(String tenantId, String customerName) {
        sendRealTimeNotification(tenantId, "NEW_CUSTOMER", "New Customer",
                "New customer registered: " + customerName,
                Map.of("customerName", customerName));
    }

    public void notifyPaymentReceived(String tenantId, String invoiceNumber, String amount) {
        sendRealTimeNotification(tenantId, "PAYMENT_RECEIVED", "Payment Received",
                "Payment of " + amount + " received for invoice " + invoiceNumber,
                Map.of("invoiceNumber", invoiceNumber, "amount", amount));
    }
}
