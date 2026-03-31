package com.saasproject.modules.pos.service;

import com.saasproject.common.exceptions.BusinessException;
import com.saasproject.common.exceptions.ResourceNotFoundException;
import com.saasproject.common.utils.CommonUtils;
import com.saasproject.modules.inventory.entity.Product;
import com.saasproject.modules.inventory.repository.ProductRepository;
import com.saasproject.modules.invoice.entity.Invoice;
import com.saasproject.modules.invoice.entity.InvoiceItem;
import com.saasproject.modules.invoice.repository.InvoiceRepository;
import com.saasproject.modules.pos.dto.PosDto;
import com.saasproject.modules.pos.entity.Cart;
import com.saasproject.modules.pos.entity.CartItem;
import com.saasproject.modules.pos.repository.CartRepository;
import com.saasproject.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * POS service for cart and checkout operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PosService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final InvoiceRepository invoiceRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Create a new cart.
     */
    @Transactional
    public PosDto.CartResponse createCart(PosDto.CustomerInfo customer) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating new cart for tenant: {}", tenantId);

        Cart cart = Cart.builder()
                .status(Cart.CartStatus.ACTIVE)
                .customerId(customer != null ? customer.getCustomerId() : null)
                .customerName(customer != null ? customer.getName() : "Walk-in Customer")
                .customerPhone(customer != null ? customer.getPhone() : null)
                .subtotal(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .build();

        cart.setTenantId(tenantId);
        cart = cartRepository.save(cart);

        log.info("Cart created: {}", cart.getId());
        return mapToCartResponse(cart);
    }

    /**
     * Get cart by ID.
     */
    @Transactional(readOnly = true)
    public PosDto.CartResponse getCart(UUID cartId) {
        String tenantId = TenantContext.getCurrentTenant();

        Cart cart = cartRepository.findByIdAndTenant(cartId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", cartId.toString()));

        return mapToCartResponse(cart);
    }

    /**
     * Add item to cart.
     */
    @Transactional
    public PosDto.CartResponse addItem(UUID cartId, PosDto.AddItemRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Adding item to cart: {}", cartId);

        Cart cart = cartRepository.findByIdAndTenant(cartId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", cartId.toString()));

        if (cart.getStatus() != Cart.CartStatus.ACTIVE) {
            throw new BusinessException("CART_NOT_ACTIVE", "Cart is not active");
        }

        Product product = productRepository.findByIdAndTenant(request.getProductId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId().toString()));

        // Check stock
        if (product.isTrackInventory() && product.getCurrentStock() < request.getQuantity()) {
            throw new BusinessException("INSUFFICIENT_STOCK",
                    "Insufficient stock. Available: " + product.getCurrentStock());
        }

        // Check if item already in cart
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Update quantity
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            existingItem.calculateAmount();
        } else {
            // Add new item
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .productId(product.getId())
                    .productName(product.getName())
                    .productSku(product.getSku())
                    .quantity(request.getQuantity())
                    .unitPrice(product.getSellingPrice())
                    .taxRate(product.getTaxRate())
                    .build();
            item.calculateAmount();
            cart.getItems().add(item);
        }

        cart.recalculateTotals();
        cart = cartRepository.save(cart);

        log.info("Item added to cart: {} - Product: {}", cartId, product.getName());
        return mapToCartResponse(cart);
    }

    /**
     * Add item by barcode.
     */
    @Transactional
    public PosDto.CartResponse addItemByBarcode(UUID cartId, String barcode, int quantity) {
        String tenantId = TenantContext.getCurrentTenant();

        Product product = productRepository.findByBarcodeAndTenant(barcode, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "barcode", barcode));

        PosDto.AddItemRequest request = new PosDto.AddItemRequest();
        request.setProductId(product.getId());
        request.setQuantity(quantity);

        return addItem(cartId, request);
    }

    /**
     * Update item quantity.
     */
    @Transactional
    public PosDto.CartResponse updateItemQuantity(UUID cartId, UUID productId, int quantity) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Updating quantity for cart: {}, product: {}", cartId, productId);

        Cart cart = cartRepository.findByIdAndTenant(cartId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", cartId.toString()));

        if (cart.getStatus() != Cart.CartStatus.ACTIVE) {
            throw new BusinessException("CART_NOT_ACTIVE", "Cart is not active");
        }

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", productId.toString()));

        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(quantity);
            item.calculateAmount();
        }

        cart.recalculateTotals();
        cart = cartRepository.save(cart);

        log.info("Item quantity updated in cart: {}", cartId);
        return mapToCartResponse(cart);
    }

    /**
     * Remove item from cart.
     */
    @Transactional
    public PosDto.CartResponse removeItem(UUID cartId, UUID productId) {
        return updateItemQuantity(cartId, productId, 0);
    }

    /**
     * Apply discount to cart.
     */
    @Transactional
    public PosDto.CartResponse applyDiscount(UUID cartId, BigDecimal discountAmount) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Applying discount {} to cart: {}", discountAmount, cartId);

        Cart cart = cartRepository.findByIdAndTenant(cartId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", cartId.toString()));

        if (cart.getStatus() != Cart.CartStatus.ACTIVE) {
            throw new BusinessException("CART_NOT_ACTIVE", "Cart is not active");
        }

        cart.setDiscountAmount(discountAmount);
        cart.recalculateTotals();
        cart = cartRepository.save(cart);

        log.info("Discount applied to cart: {}", cartId);
        return mapToCartResponse(cart);
    }

    /**
     * Checkout cart.
     */
    @Transactional
    public PosDto.CheckoutResponse checkout(UUID cartId, PosDto.CheckoutRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Processing checkout for cart: {}", cartId);

        Cart cart = cartRepository.findByIdAndTenant(cartId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", cartId.toString()));

        if (cart.getStatus() != Cart.CartStatus.ACTIVE) {
            throw new BusinessException("CART_NOT_ACTIVE", "Cart is not active");
        }

        if (cart.getItems().isEmpty()) {
            throw new BusinessException("EMPTY_CART", "Cart is empty");
        }

        // Create invoice
        Invoice invoice = Invoice.builder()
                .invoiceNumber(CommonUtils.generateNumber("INV"))
                .customerId(cart.getCustomerId())
                .customerName(cart.getCustomerName())
                .customerPhone(cart.getCustomerPhone())
                .invoiceDate(LocalDate.now())
                .status(Invoice.InvoiceStatus.PAID)
                .subtotal(cart.getSubtotal())
                .taxAmount(cart.getTaxAmount())
                .discountAmount(cart.getDiscountAmount())
                .totalAmount(cart.getTotalAmount())
                .paidAmount(cart.getTotalAmount())
                .balanceDue(BigDecimal.ZERO)
                .paymentMethod(Invoice.PaymentMethod.valueOf(request.getPaymentMethod()))
                .build();
        invoice.setTenantId(tenantId);

        // Add invoice items
        for (CartItem cartItem : cart.getItems()) {
            InvoiceItem invoiceItem = InvoiceItem.builder()
                    .productId(cartItem.getProductId())
                    .productName(cartItem.getProductName())
                    .productSku(cartItem.getProductSku())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .taxRate(cartItem.getTaxRate())
                    .taxAmount(cartItem.getTaxAmount())
                    .amount(cartItem.getAmount())
                    .build();
            invoice.addItem(invoiceItem);

            // Deduct stock
            productRepository.findById(cartItem.getProductId()).ifPresent(product -> {
                if (product.isTrackInventory()) {
                    product.adjustStock(-cartItem.getQuantity());
                    productRepository.save(product);
                }
            });
        }

        invoice = invoiceRepository.save(invoice);

        // Update cart status
        cart.setStatus(Cart.CartStatus.COMPLETED);
        cart.setInvoiceId(invoice.getId());
        cart.setPaymentMethod(Invoice.PaymentMethod.valueOf(request.getPaymentMethod()));
        cartRepository.save(cart);

        // Calculate change
        BigDecimal change = BigDecimal.ZERO;
        if (request.getReceivedAmount() != null &&
                request.getReceivedAmount().compareTo(cart.getTotalAmount()) > 0) {
            change = request.getReceivedAmount().subtract(cart.getTotalAmount());
        }

        // Broadcast checkout event
        messagingTemplate.convertAndSend("/topic/pos/" + tenantId, Map.of(
                "type", "CHECKOUT_COMPLETE",
                "cartId", cartId,
                "invoiceNumber", invoice.getInvoiceNumber(),
                "totalAmount", cart.getTotalAmount()));

        log.info("Checkout complete. Invoice: {}", invoice.getInvoiceNumber());

        return PosDto.CheckoutResponse.builder()
                .cartId(cartId.toString())
                .invoiceId(invoice.getId().toString())
                .invoiceNumber(invoice.getInvoiceNumber())
                .totalAmount(cart.getTotalAmount())
                .paidAmount(request.getReceivedAmount() != null ? request.getReceivedAmount() : cart.getTotalAmount())
                .change(change)
                .paymentMethod(request.getPaymentMethod())
                .status("COMPLETED")
                .build();
    }

    /**
     * Void/cancel cart.
     */
    @Transactional
    public void voidCart(UUID cartId) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Voiding cart: {}", cartId);

        Cart cart = cartRepository.findByIdAndTenant(cartId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", cartId.toString()));

        if (cart.getStatus() == Cart.CartStatus.COMPLETED) {
            throw new BusinessException("CANNOT_VOID", "Cannot void a completed cart");
        }

        cart.setStatus(Cart.CartStatus.VOID);
        cartRepository.save(cart);

        log.info("Cart voided: {}", cartId);
    }

    private PosDto.CartResponse mapToCartResponse(Cart cart) {
        return PosDto.CartResponse.builder()
                .id(cart.getId().toString())
                .status(cart.getStatus().name())
                .customerName(cart.getCustomerName())
                .customerPhone(cart.getCustomerPhone())
                .items(cart.getItems().stream()
                        .map(this::mapToItemResponse)
                        .collect(Collectors.toList()))
                .subtotal(cart.getSubtotal())
                .taxAmount(cart.getTaxAmount())
                .discountAmount(cart.getDiscountAmount())
                .totalAmount(cart.getTotalAmount())
                .itemCount(cart.getItems().stream().mapToInt(CartItem::getQuantity).sum())
                .createdAt(CommonUtils.formatDateTime(cart.getCreatedAt()))
                .build();
    }

    private PosDto.CartItemResponse mapToItemResponse(CartItem item) {
        return PosDto.CartItemResponse.builder()
                .productId(item.getProductId().toString())
                .productName(item.getProductName())
                .productSku(item.getProductSku())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .taxRate(item.getTaxRate())
                .taxAmount(item.getTaxAmount())
                .amount(item.getAmount())
                .build();
    }
}
