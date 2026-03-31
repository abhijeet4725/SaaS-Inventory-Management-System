package com.saasproject.modules.inventory.service;

import com.saasproject.common.exceptions.BusinessException;
import com.saasproject.common.exceptions.ResourceNotFoundException;
import com.saasproject.modules.inventory.dto.ProductDto;
import com.saasproject.modules.inventory.entity.Product;
import com.saasproject.modules.inventory.entity.StockMovement;
import com.saasproject.modules.inventory.mapper.ProductMapper;
import com.saasproject.modules.inventory.repository.ProductRepository;
import com.saasproject.modules.inventory.repository.StockMovementRepository;
import com.saasproject.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Inventory service for product and stock management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductMapper productMapper;

    /**
     * Create a new product.
     */
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductDto.Response createProduct(ProductDto.CreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating product: {} for tenant: {}", request.getName(), tenantId);

        // Check for duplicate SKU
        if (request.getSku() != null &&
                productRepository.existsBySkuAndTenantIdAndDeletedFalse(request.getSku(), tenantId)) {
            throw new BusinessException("DUPLICATE_SKU", "SKU already exists: " + request.getSku());
        }

        // Check for duplicate barcode
        if (request.getBarcode() != null &&
                productRepository.existsByBarcodeAndTenantIdAndDeletedFalse(request.getBarcode(), tenantId)) {
            throw new BusinessException("DUPLICATE_BARCODE", "Barcode already exists: " + request.getBarcode());
        }

        Product product = productMapper.toEntity(request);
        product.setTenantId(tenantId);
        product = productRepository.save(product);

        log.info("Product created: {}", product.getId());
        return productMapper.toResponse(product);
    }

    /**
     * Get product by ID.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public ProductDto.Response getProduct(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();

        Product product = productRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id.toString()));

        return productMapper.toResponse(product);
    }

    /**
     * Get products with pagination.
     */
    @Transactional(readOnly = true)
    public Page<ProductDto.Response> getProducts(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();

        return productRepository.findByTenant(tenantId, pageable)
                .map(productMapper::toResponse);
    }

    /**
     * Search products.
     */
    @Transactional(readOnly = true)
    public Page<ProductDto.Response> searchProducts(String query, Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();

        return productRepository.search(tenantId, query, pageable)
                .map(productMapper::toResponse);
    }

    /**
     * Update product.
     */
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public ProductDto.Response updateProduct(UUID id, ProductDto.UpdateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Updating product: {} for tenant: {}", id, tenantId);

        Product product = productRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id.toString()));

        productMapper.updateEntity(product, request);
        product = productRepository.save(product);

        log.info("Product updated: {}", product.getId());
        return productMapper.toResponse(product);
    }

    /**
     * Update stock levels.
     */
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public ProductDto.Response updateStock(UUID id, ProductDto.StockUpdateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Updating stock for product: {} by {}", id, request.getQuantity());

        Product product = productRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id.toString()));

        if (!product.isTrackInventory()) {
            throw new BusinessException("NO_INVENTORY_TRACKING",
                    "Inventory tracking is disabled for this product");
        }

        int stockBefore = product.getCurrentStock();
        product.adjustStock(request.getQuantity());

        if (product.getCurrentStock() < 0) {
            throw new BusinessException("INSUFFICIENT_STOCK",
                    "Insufficient stock. Current: " + stockBefore + ", Requested: " + Math.abs(request.getQuantity()));
        }

        // Record stock movement
        StockMovement movement = StockMovement.builder()
                .product(product)
                .movementType(parseMovementType(request.getReason()))
                .quantity(request.getQuantity())
                .stockBefore(stockBefore)
                .stockAfter(product.getCurrentStock())
                .notes(request.getNotes())
                .build();
        movement.setTenantId(tenantId);

        stockMovementRepository.save(movement);
        product = productRepository.save(product);

        log.info("Stock updated for product: {}. Before: {}, After: {}",
                id, stockBefore, product.getCurrentStock());

        return productMapper.toResponse(product);
    }

    /**
     * Get low stock products.
     */
    @Transactional(readOnly = true)
    public List<ProductDto.Response> getLowStockProducts() {
        String tenantId = TenantContext.getCurrentTenant();

        return productRepository.findLowStockProducts(tenantId).stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Delete product (soft delete).
     */
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Deleting product: {} for tenant: {}", id, tenantId);

        Product product = productRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id.toString()));

        product.softDelete();
        productRepository.save(product);

        log.info("Product soft deleted: {}", id);
    }

    /**
     * Lookup product by barcode.
     */
    @Transactional(readOnly = true)
    public ProductDto.Response getByBarcode(String barcode) {
        String tenantId = TenantContext.getCurrentTenant();

        Product product = productRepository.findByBarcodeAndTenant(barcode, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "barcode", barcode));

        return productMapper.toResponse(product);
    }

    private StockMovement.MovementType parseMovementType(String reason) {
        try {
            return StockMovement.MovementType.valueOf(reason.toUpperCase());
        } catch (IllegalArgumentException e) {
            return StockMovement.MovementType.ADJUSTMENT;
        }
    }
}
