package com.saasproject.modules.purchase.service;

import com.saasproject.common.exceptions.BusinessException;
import com.saasproject.common.exceptions.ResourceNotFoundException;
import com.saasproject.common.utils.CommonUtils;
import com.saasproject.modules.inventory.entity.Product;
import com.saasproject.modules.inventory.repository.ProductRepository;
import com.saasproject.modules.purchase.dto.PurchaseOrderDto;
import com.saasproject.modules.purchase.entity.PurchaseOrder;
import com.saasproject.modules.purchase.entity.PurchaseOrderItem;
import com.saasproject.modules.purchase.repository.PurchaseOrderRepository;
import com.saasproject.modules.supplier.entity.Supplier;
import com.saasproject.modules.supplier.repository.SupplierRepository;
import com.saasproject.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Purchase Order service for procurement management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository poRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    /**
     * Create a new purchase order.
     */
    @Transactional
    public PurchaseOrderDto.Response createPurchaseOrder(PurchaseOrderDto.CreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating purchase order for tenant: {}", tenantId);

        // Validate supplier
        UUID supplierId = UUID.fromString(request.getSupplierId());
        Supplier supplier = supplierRepository.findByIdAndTenant(supplierId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", request.getSupplierId()));

        // Create PO
        PurchaseOrder po = PurchaseOrder.builder()
                .poNumber(CommonUtils.generateNumber("PO"))
                .supplierId(supplierId)
                .supplierName(supplier.getName())
                .supplierEmail(supplier.getEmail())
                .orderDate(LocalDate.now())
                .expectedDate(request.getExpectedDate() != null ? LocalDate.parse(request.getExpectedDate()) : null)
                .shippingCost(request.getShippingCost() != null ? request.getShippingCost() : java.math.BigDecimal.ZERO)
                .discountAmount(
                        request.getDiscountAmount() != null ? request.getDiscountAmount() : java.math.BigDecimal.ZERO)
                .shippingAddress(request.getShippingAddress())
                .notes(request.getNotes())
                .status(PurchaseOrder.POStatus.DRAFT)
                .build();
        po.setTenantId(tenantId);

        // Add items
        for (PurchaseOrderDto.ItemRequest itemRequest : request.getItems()) {
            PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .productId(itemRequest.getProductId() != null ? UUID.fromString(itemRequest.getProductId()) : null)
                    .productName(itemRequest.getProductName())
                    .productSku(itemRequest.getProductSku())
                    .description(itemRequest.getDescription())
                    .quantity(itemRequest.getQuantity())
                    .unitCost(itemRequest.getUnitCost())
                    .taxRate(itemRequest.getTaxRate() != null ? itemRequest.getTaxRate() : java.math.BigDecimal.ZERO)
                    .build();
            item.calculateAmount();
            po.addItem(item);
        }

        po.recalculateTotals();
        po = poRepository.save(po);

        log.info("Purchase order created: {}", po.getPoNumber());
        return mapToResponse(po);
    }

    /**
     * Get purchase order by ID.
     */
    @Transactional(readOnly = true)
    public PurchaseOrderDto.Response getPurchaseOrder(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();

        PurchaseOrder po = poRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id.toString()));

        return mapToResponse(po);
    }

    /**
     * List purchase orders.
     */
    @Transactional(readOnly = true)
    public Page<PurchaseOrderDto.Summary> getPurchaseOrders(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return poRepository.findByTenant(tenantId, pageable)
                .map(this::mapToSummary);
    }

    /**
     * Search purchase orders.
     */
    @Transactional(readOnly = true)
    public Page<PurchaseOrderDto.Summary> searchPurchaseOrders(String query, Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return poRepository.search(tenantId, query, pageable)
                .map(this::mapToSummary);
    }

    /**
     * Approve purchase order.
     */
    @Transactional
    public PurchaseOrderDto.Response approvePurchaseOrder(UUID id, String approverEmail) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Approving PO: {}", id);

        PurchaseOrder po = poRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id.toString()));

        if (po.getStatus() != PurchaseOrder.POStatus.DRAFT &&
                po.getStatus() != PurchaseOrder.POStatus.PENDING_APPROVAL) {
            throw new BusinessException("INVALID_STATUS", "Cannot approve PO in status: " + po.getStatus());
        }

        po.approve(approverEmail);
        po = poRepository.save(po);

        log.info("PO approved: {}", po.getPoNumber());
        return mapToResponse(po);
    }

    /**
     * Mark PO as sent to supplier.
     */
    @Transactional
    public PurchaseOrderDto.Response sendPurchaseOrder(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Sending PO: {}", id);

        PurchaseOrder po = poRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id.toString()));

        if (po.getStatus() != PurchaseOrder.POStatus.APPROVED) {
            throw new BusinessException("INVALID_STATUS", "PO must be approved before sending");
        }

        po.markSent();
        po = poRepository.save(po);

        // TODO: Send email to supplier
        log.info("PO sent: {}", po.getPoNumber());
        return mapToResponse(po);
    }

    /**
     * Receive items from PO.
     */
    @Transactional
    public PurchaseOrderDto.Response receiveItems(UUID id, PurchaseOrderDto.ReceiveRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Receiving items for PO: {}", id);

        PurchaseOrder po = poRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id.toString()));

        if (po.getStatus() != PurchaseOrder.POStatus.SENT &&
                po.getStatus() != PurchaseOrder.POStatus.PARTIALLY_RECEIVED) {
            throw new BusinessException("INVALID_STATUS", "Cannot receive items for PO in status: " + po.getStatus());
        }

        // Process each item
        for (PurchaseOrderDto.ReceiveItemRequest itemReq : request.getItems()) {
            UUID itemId = UUID.fromString(itemReq.getItemId());
            PurchaseOrderItem item = po.getItems().stream()
                    .filter(i -> i.getId().equals(itemId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrderItem", itemReq.getItemId()));

            item.receiveQuantity(itemReq.getQuantity());

            // Update product stock if product is linked
            if (item.getProductId() != null) {
                productRepository.findById(item.getProductId()).ifPresent(product -> {
                    product.adjustStock(itemReq.getQuantity());
                    productRepository.save(product);
                });
            }
        }

        // Update PO status
        boolean allReceived = po.getItems().stream().allMatch(PurchaseOrderItem::isFullyReceived);
        if (allReceived) {
            po.markReceived();
        } else {
            po.setStatus(PurchaseOrder.POStatus.PARTIALLY_RECEIVED);
        }

        po = poRepository.save(po);
        log.info("Items received for PO: {}", po.getPoNumber());
        return mapToResponse(po);
    }

    /**
     * Cancel purchase order.
     */
    @Transactional
    public void cancelPurchaseOrder(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Cancelling PO: {}", id);

        PurchaseOrder po = poRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id.toString()));

        if (po.getStatus() == PurchaseOrder.POStatus.RECEIVED) {
            throw new BusinessException("CANNOT_CANCEL", "Cannot cancel a received PO");
        }

        po.setStatus(PurchaseOrder.POStatus.CANCELLED);
        poRepository.save(po);

        log.info("PO cancelled: {}", po.getPoNumber());
    }

    /**
     * Get overdue POs.
     */
    @Transactional(readOnly = true)
    public List<PurchaseOrderDto.Summary> getOverduePurchaseOrders() {
        String tenantId = TenantContext.getCurrentTenant();
        return poRepository.findOverdue(tenantId, LocalDate.now()).stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    // ===== Mapping Methods =====

    private PurchaseOrderDto.Response mapToResponse(PurchaseOrder po) {
        return PurchaseOrderDto.Response.builder()
                .id(po.getId().toString())
                .poNumber(po.getPoNumber())
                .supplierId(po.getSupplierId().toString())
                .supplierName(po.getSupplierName())
                .supplierEmail(po.getSupplierEmail())
                .orderDate(po.getOrderDate().toString())
                .expectedDate(po.getExpectedDate() != null ? po.getExpectedDate().toString() : null)
                .receivedDate(po.getReceivedDate() != null ? po.getReceivedDate().toString() : null)
                .status(po.getStatus().name())
                .items(po.getItems().stream().map(this::mapItemToResponse).collect(Collectors.toList()))
                .subtotal(po.getSubtotal())
                .taxAmount(po.getTaxAmount())
                .shippingCost(po.getShippingCost())
                .discountAmount(po.getDiscountAmount())
                .totalAmount(po.getTotalAmount())
                .shippingAddress(po.getShippingAddress())
                .notes(po.getNotes())
                .approvedBy(po.getApprovedBy())
                .createdAt(CommonUtils.formatDateTime(po.getCreatedAt()))
                .updatedAt(CommonUtils.formatDateTime(po.getUpdatedAt()))
                .build();
    }

    private PurchaseOrderDto.ItemResponse mapItemToResponse(PurchaseOrderItem item) {
        return PurchaseOrderDto.ItemResponse.builder()
                .id(item.getId().toString())
                .productId(item.getProductId() != null ? item.getProductId().toString() : null)
                .productName(item.getProductName())
                .productSku(item.getProductSku())
                .description(item.getDescription())
                .quantity(item.getQuantity())
                .receivedQuantity(item.getReceivedQuantity())
                .pendingQuantity(item.getPendingQuantity())
                .unitCost(item.getUnitCost())
                .taxRate(item.getTaxRate())
                .taxAmount(item.getTaxAmount())
                .amount(item.getAmount())
                .build();
    }

    private PurchaseOrderDto.Summary mapToSummary(PurchaseOrder po) {
        return PurchaseOrderDto.Summary.builder()
                .id(po.getId().toString())
                .poNumber(po.getPoNumber())
                .supplierName(po.getSupplierName())
                .orderDate(po.getOrderDate().toString())
                .status(po.getStatus().name())
                .totalAmount(po.getTotalAmount())
                .itemCount(po.getItems().size())
                .build();
    }
}
