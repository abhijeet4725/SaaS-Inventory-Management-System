package com.saasproject.modules.supplier.controller;

import com.saasproject.common.api_response.ApiResponse;
import com.saasproject.common.dto.PageRequestDto;
import com.saasproject.modules.supplier.dto.SupplierDto;
import com.saasproject.modules.supplier.service.SupplierService;
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
 * Supplier controller for vendor management.
 */
@RestController
@RequestMapping("/v1/suppliers")
@RequiredArgsConstructor
@Tag(name = "Suppliers", description = "Vendor/Supplier management")
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create supplier", description = "Add a new supplier")
    public ResponseEntity<ApiResponse<SupplierDto.Response>> createSupplier(
            @Valid @RequestBody SupplierDto.CreateRequest request) {

        SupplierDto.Response response = supplierService.createSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @GetMapping
    @Operation(summary = "List suppliers", description = "Get paginated list of suppliers")
    public ResponseEntity<ApiResponse<List<SupplierDto.Response>>> getSuppliers(
            @ModelAttribute PageRequestDto pageRequest) {

        Page<SupplierDto.Response> page = supplierService.getSuppliers(pageRequest.toPageable());
        return ResponseEntity.ok(ApiResponse.paginated(
                page.getContent(),
                ApiResponse.PageInfo.of(page)));
    }

    @GetMapping("/active")
    @Operation(summary = "List active suppliers", description = "Get all active suppliers (for dropdowns)")
    public ResponseEntity<ApiResponse<List<SupplierDto.Response>>> getActiveSuppliers() {

        List<SupplierDto.Response> suppliers = supplierService.getActiveSuppliers();
        return ResponseEntity.ok(ApiResponse.success(suppliers));
    }

    @GetMapping("/search")
    @Operation(summary = "Search suppliers", description = "Search suppliers by name or code")
    public ResponseEntity<ApiResponse<List<SupplierDto.Response>>> searchSuppliers(
            @RequestParam String query,
            @ModelAttribute PageRequestDto pageRequest) {

        Page<SupplierDto.Response> page = supplierService.searchSuppliers(query, pageRequest.toPageable());
        return ResponseEntity.ok(ApiResponse.paginated(
                page.getContent(),
                ApiResponse.PageInfo.of(page)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier", description = "Get supplier by ID")
    public ResponseEntity<ApiResponse<SupplierDto.Response>> getSupplier(
            @PathVariable UUID id) {

        SupplierDto.Response response = supplierService.getSupplier(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update supplier", description = "Update supplier details")
    public ResponseEntity<ApiResponse<SupplierDto.Response>> updateSupplier(
            @PathVariable UUID id,
            @Valid @RequestBody SupplierDto.UpdateRequest request) {

        SupplierDto.Response response = supplierService.updateSupplier(id, request);
        return ResponseEntity.ok(ApiResponse.success("Supplier updated", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete supplier", description = "Soft delete a supplier")
    public ResponseEntity<ApiResponse<Void>> deleteSupplier(@PathVariable UUID id) {

        supplierService.deleteSupplier(id);
        return ResponseEntity.ok(ApiResponse.success("Supplier deleted"));
    }
}
