package com.saasproject.modules.inventory.controller;

import com.saasproject.common.api_response.ApiResponse;
import com.saasproject.common.dto.PageRequestDto;
import com.saasproject.modules.inventory.dto.ProductDto;
import com.saasproject.modules.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Inventory controller for product and stock management.
 */
@RestController
@RequestMapping("/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Product and stock management")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/products")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create product", description = "Add a new product to inventory")
    public ResponseEntity<ApiResponse<ProductDto.Response>> createProduct(
            @Valid @RequestBody ProductDto.CreateRequest request) {

        ProductDto.Response response = inventoryService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @GetMapping("/products")
    @Operation(summary = "List products", description = "Get paginated list of products")
    public ResponseEntity<ApiResponse<List<ProductDto.Response>>> getProducts(
            @ModelAttribute PageRequestDto pageRequest) {

        Page<ProductDto.Response> page = inventoryService.getProducts(pageRequest.toPageable());
        return ResponseEntity.ok(ApiResponse.paginated(
                page.getContent(),
                ApiResponse.PageInfo.of(page)));
    }

    @GetMapping("/products/{id}")
    @Operation(summary = "Get product", description = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductDto.Response>> getProduct(
            @PathVariable UUID id) {

        ProductDto.Response response = inventoryService.getProduct(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/products/search")
    @Operation(summary = "Search products", description = "Search products by name, SKU, or barcode")
    public ResponseEntity<ApiResponse<List<ProductDto.Response>>> searchProducts(
            @RequestParam String query,
            @ModelAttribute PageRequestDto pageRequest) {

        Page<ProductDto.Response> page = inventoryService.searchProducts(query, pageRequest.toPageable());
        return ResponseEntity.ok(ApiResponse.paginated(
                page.getContent(),
                ApiResponse.PageInfo.of(page)));
    }

    @GetMapping("/products/barcode/{barcode}")
    @Operation(summary = "Get by barcode", description = "Look up product by barcode (for POS)")
    public ResponseEntity<ApiResponse<ProductDto.Response>> getByBarcode(
            @PathVariable String barcode) {

        ProductDto.Response response = inventoryService.getByBarcode(barcode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/products/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update product", description = "Update product details")
    public ResponseEntity<ApiResponse<ProductDto.Response>> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductDto.UpdateRequest request) {

        ProductDto.Response response = inventoryService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated", response));
    }

    @PutMapping("/products/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update stock", description = "Adjust product stock levels")
    public ResponseEntity<ApiResponse<ProductDto.Response>> updateStock(
            @PathVariable UUID id,
            @Valid @RequestBody ProductDto.StockUpdateRequest request) {

        ProductDto.Response response = inventoryService.updateStock(id, request);
        return ResponseEntity.ok(ApiResponse.success("Stock updated", response));
    }

    @GetMapping("/products/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Low stock alert", description = "Get products below minimum stock level")
    public ResponseEntity<ApiResponse<List<ProductDto.Response>>> getLowStockProducts() {

        List<ProductDto.Response> products = inventoryService.getLowStockProducts();
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete product", description = "Soft delete a product")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable UUID id) {

        inventoryService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted"));
    }
}
