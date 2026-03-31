package com.saasproject.modules.pos.controller;

import com.saasproject.common.api_response.ApiResponse;
import com.saasproject.modules.pos.dto.PosDto;
import com.saasproject.modules.pos.service.PosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * POS controller for point-of-sale operations.
 */
@RestController
@RequestMapping("/v1/pos")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
@Tag(name = "POS", description = "Point of Sale operations")
public class PosController {

    private final PosService posService;

    @PostMapping("/cart")
    @Operation(summary = "Create cart", description = "Create a new cart")
    public ResponseEntity<ApiResponse<PosDto.CartResponse>> createCart(
            @RequestBody(required = false) PosDto.CustomerInfo customer) {

        PosDto.CartResponse response = posService.createCart(customer);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @GetMapping("/cart/{cartId}")
    @Operation(summary = "Get cart", description = "Get cart by ID")
    public ResponseEntity<ApiResponse<PosDto.CartResponse>> getCart(
            @PathVariable UUID cartId) {

        PosDto.CartResponse response = posService.getCart(cartId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/cart/{cartId}/items")
    @Operation(summary = "Add item", description = "Add item to cart")
    public ResponseEntity<ApiResponse<PosDto.CartResponse>> addItem(
            @PathVariable UUID cartId,
            @Valid @RequestBody PosDto.AddItemRequest request) {

        PosDto.CartResponse response = posService.addItem(cartId, request);
        return ResponseEntity.ok(ApiResponse.success("Item added", response));
    }

    @PostMapping("/cart/{cartId}/items/barcode")
    @Operation(summary = "Add by barcode", description = "Add item by barcode")
    public ResponseEntity<ApiResponse<PosDto.CartResponse>> addItemByBarcode(
            @PathVariable UUID cartId,
            @RequestParam String barcode,
            @RequestParam(defaultValue = "1") int quantity) {

        PosDto.CartResponse response = posService.addItemByBarcode(cartId, barcode, quantity);
        return ResponseEntity.ok(ApiResponse.success("Item added", response));
    }

    @PutMapping("/cart/{cartId}/items/{productId}")
    @Operation(summary = "Update quantity", description = "Update item quantity")
    public ResponseEntity<ApiResponse<PosDto.CartResponse>> updateItemQuantity(
            @PathVariable UUID cartId,
            @PathVariable UUID productId,
            @RequestParam int quantity) {

        PosDto.CartResponse response = posService.updateItemQuantity(cartId, productId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Quantity updated", response));
    }

    @DeleteMapping("/cart/{cartId}/items/{productId}")
    @Operation(summary = "Remove item", description = "Remove item from cart")
    public ResponseEntity<ApiResponse<PosDto.CartResponse>> removeItem(
            @PathVariable UUID cartId,
            @PathVariable UUID productId) {

        PosDto.CartResponse response = posService.removeItem(cartId, productId);
        return ResponseEntity.ok(ApiResponse.success("Item removed", response));
    }

    @PostMapping("/cart/{cartId}/discount")
    @Operation(summary = "Apply discount", description = "Apply discount to cart")
    public ResponseEntity<ApiResponse<PosDto.CartResponse>> applyDiscount(
            @PathVariable UUID cartId,
            @RequestParam BigDecimal amount) {

        PosDto.CartResponse response = posService.applyDiscount(cartId, amount);
        return ResponseEntity.ok(ApiResponse.success("Discount applied", response));
    }

    @PostMapping("/cart/{cartId}/checkout")
    @Operation(summary = "Checkout", description = "Complete checkout and create invoice")
    public ResponseEntity<ApiResponse<PosDto.CheckoutResponse>> checkout(
            @PathVariable UUID cartId,
            @Valid @RequestBody PosDto.CheckoutRequest request) {

        PosDto.CheckoutResponse response = posService.checkout(cartId, request);
        return ResponseEntity.ok(ApiResponse.success("Checkout complete", response));
    }

    @PostMapping("/cart/{cartId}/void")
    @Operation(summary = "Void cart", description = "Cancel/void the cart")
    public ResponseEntity<ApiResponse<Void>> voidCart(@PathVariable UUID cartId) {

        posService.voidCart(cartId);
        return ResponseEntity.ok(ApiResponse.success("Cart voided"));
    }
}
