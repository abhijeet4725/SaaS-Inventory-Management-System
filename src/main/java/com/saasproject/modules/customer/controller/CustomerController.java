package com.saasproject.modules.customer.controller;

import com.saasproject.common.api_response.ApiResponse;
import com.saasproject.common.dto.PageRequestDto;
import com.saasproject.modules.customer.dto.CustomerDto;
import com.saasproject.modules.customer.service.CustomerService;
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
 * Customer controller for customer management.
 */
@RestController
@RequestMapping("/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer management")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Create customer", description = "Add a new customer")
    public ResponseEntity<ApiResponse<CustomerDto.Response>> createCustomer(
            @Valid @RequestBody CustomerDto.CreateRequest request) {

        CustomerDto.Response response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @GetMapping
    @Operation(summary = "List customers", description = "Get paginated list of customers")
    public ResponseEntity<ApiResponse<List<CustomerDto.Response>>> getCustomers(
            @ModelAttribute PageRequestDto pageRequest) {

        Page<CustomerDto.Response> page = customerService.getCustomers(pageRequest.toPageable());
        return ResponseEntity.ok(ApiResponse.paginated(
                page.getContent(),
                ApiResponse.PageInfo.of(page)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search customers", description = "Search by name, email, or phone")
    public ResponseEntity<ApiResponse<List<CustomerDto.Response>>> searchCustomers(
            @RequestParam String query,
            @ModelAttribute PageRequestDto pageRequest) {

        Page<CustomerDto.Response> page = customerService.searchCustomers(query, pageRequest.toPageable());
        return ResponseEntity.ok(ApiResponse.paginated(
                page.getContent(),
                ApiResponse.PageInfo.of(page)));
    }

    @GetMapping("/phone/{phone}")
    @Operation(summary = "Find by phone", description = "Find customer by phone number")
    public ResponseEntity<ApiResponse<CustomerDto.Response>> findByPhone(
            @PathVariable String phone) {

        CustomerDto.Response response = customerService.findByPhone(phone);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer", description = "Get customer by ID")
    public ResponseEntity<ApiResponse<CustomerDto.Response>> getCustomer(
            @PathVariable UUID id) {

        CustomerDto.Response response = customerService.getCustomer(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update customer", description = "Update customer details")
    public ResponseEntity<ApiResponse<CustomerDto.Response>> updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerDto.UpdateRequest request) {

        CustomerDto.Response response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Customer updated", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete customer", description = "Soft delete a customer")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable UUID id) {

        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Customer deleted"));
    }
}
