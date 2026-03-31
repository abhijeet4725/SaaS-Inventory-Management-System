package com.saasproject.modules.payment.controller;

import com.saasproject.common.api_response.ApiResponse;
import com.saasproject.modules.payment.service.PaymentGatewayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Payment controller for payment gateway integration.
 */
@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment gateway operations")
public class PaymentController {

    private final PaymentGatewayService paymentService;

    @PostMapping("/create-order")
    @Operation(summary = "Create payment order", description = "Create a new payment order/intent")
    public ResponseEntity<ApiResponse<PaymentGatewayService.PaymentOrder>> createOrder(
            @RequestBody CreateOrderRequest request) {

        PaymentGatewayService.PaymentOrder order = paymentService.createPaymentOrder(
                request.getAmount(),
                request.getCurrency(),
                request.getDescription(),
                request.getMetadata());

        return ResponseEntity.ok(ApiResponse.success("Payment order created", order));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify payment", description = "Verify payment signature/webhook")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyPayment(
            @RequestBody VerifyPaymentRequest request) {

        boolean verified = paymentService.verifyPayment(
                request.getOrderId(),
                request.getPaymentId(),
                request.getSignature());

        if (verified) {
            return ResponseEntity.ok(ApiResponse.success("Payment verified",
                    Map.of("verified", true, "orderId", request.getOrderId())));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Payment verification failed"));
        }
    }

    @PostMapping("/refund")
    @Operation(summary = "Process refund", description = "Process a payment refund")
    public ResponseEntity<ApiResponse<PaymentGatewayService.RefundResult>> processRefund(
            @RequestBody RefundRequest request) {

        PaymentGatewayService.RefundResult result = paymentService.processRefund(
                request.getPaymentId(),
                request.getAmount(),
                request.getReason());

        return ResponseEntity.ok(ApiResponse.success("Refund processed", result));
    }

    // ===== Request DTOs =====

    @lombok.Data
    public static class CreateOrderRequest {
        private BigDecimal amount;
        private String currency = "INR";
        private String description;
        private Map<String, String> metadata;
    }

    @lombok.Data
    public static class VerifyPaymentRequest {
        private String orderId;
        private String paymentId;
        private String signature;
    }

    @lombok.Data
    public static class RefundRequest {
        private String paymentId;
        private BigDecimal amount;
        private String reason;
    }
}
