package com.saasproject.modules.purchase.controller;

import com.saasproject.common.api_response.ApiResponse;
import com.saasproject.common.dto.PageRequestDto;
import com.saasproject.modules.purchase.dto.PurchaseOrderDto;
import com.saasproject.modules.purchase.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Purchase Order controller for procurement management.
 */
@RestController
@RequestMapping("/v1/purchase-orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@Tag(name = "Purchase Orders", description = "Procurement/Purchase Order management")
public class PurchaseOrderController {

    private final PurchaseOrderService poService;

    @PostMapping
    @Operation(summary = "Create PO", description = "Create a new purchase order")
    public ResponseEntity<ApiResponse<PurchaseOrderDto.Response>> createPurchaseOrder(
            @Valid @RequestBody PurchaseOrderDto.CreateRequest request) {

        PurchaseOrderDto.Response response = poService.createPurchaseOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @GetMapping
    @Operation(summary = "List POs", description = "Get paginated list of purchase orders")
    public ResponseEntity<ApiResponse<List<PurchaseOrderDto.Summary>>> getPurchaseOrders(
            @ModelAttribute PageRequestDto pageRequest) {

        Page<PurchaseOrderDto.Summary> page = poService.getPurchaseOrders(pageRequest.toPageable());
        return ResponseEntity.ok(ApiResponse.paginated(
                page.getContent(),
                ApiResponse.PageInfo.of(page)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search POs", description = "Search by PO number or supplier name")
    public ResponseEntity<ApiResponse<List<PurchaseOrderDto.Summary>>> searchPurchaseOrders(
            @RequestParam String query,
            @ModelAttribute PageRequestDto pageRequest) {

        Page<PurchaseOrderDto.Summary> page = poService.searchPurchaseOrders(query, pageRequest.toPageable());
        return ResponseEntity.ok(ApiResponse.paginated(
                page.getContent(),
                ApiResponse.PageInfo.of(page)));
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get overdue POs", description = "Get purchase orders past expected date")
    public ResponseEntity<ApiResponse<List<PurchaseOrderDto.Summary>>> getOverduePurchaseOrders() {

        List<PurchaseOrderDto.Summary> orders = poService.getOverduePurchaseOrders();
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get PO", description = "Get purchase order by ID")
    public ResponseEntity<ApiResponse<PurchaseOrderDto.Response>> getPurchaseOrder(
            @PathVariable UUID id) {

        PurchaseOrderDto.Response response = poService.getPurchaseOrder(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve PO", description = "Approve a purchase order")
    public ResponseEntity<ApiResponse<PurchaseOrderDto.Response>> approvePurchaseOrder(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {

        PurchaseOrderDto.Response response = poService.approvePurchaseOrder(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Purchase order approved", response));
    }

    @PostMapping("/{id}/send")
    @Operation(summary = "Send PO", description = "Mark PO as sent to supplier")
    public ResponseEntity<ApiResponse<PurchaseOrderDto.Response>> sendPurchaseOrder(
            @PathVariable UUID id) {

        PurchaseOrderDto.Response response = poService.sendPurchaseOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Purchase order sent", response));
    }

    @PostMapping("/{id}/receive")
    @Operation(summary = "Receive items", description = "Receive items from purchase order")
    public ResponseEntity<ApiResponse<PurchaseOrderDto.Response>> receiveItems(
            @PathVariable UUID id,
            @Valid @RequestBody PurchaseOrderDto.ReceiveRequest request) {

        PurchaseOrderDto.Response response = poService.receiveItems(id, request);
        return ResponseEntity.ok(ApiResponse.success("Items received", response));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel PO", description = "Cancel a purchase order")
    public ResponseEntity<ApiResponse<Void>> cancelPurchaseOrder(@PathVariable UUID id) {

        poService.cancelPurchaseOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Purchase order cancelled"));
    }
}
