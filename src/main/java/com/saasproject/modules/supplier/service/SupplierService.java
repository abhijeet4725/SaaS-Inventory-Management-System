package com.saasproject.modules.supplier.service;

import com.saasproject.common.exceptions.BusinessException;
import com.saasproject.common.exceptions.ResourceNotFoundException;
import com.saasproject.common.utils.CommonUtils;
import com.saasproject.modules.supplier.dto.SupplierDto;
import com.saasproject.modules.supplier.entity.Supplier;
import com.saasproject.modules.supplier.repository.SupplierRepository;
import com.saasproject.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Supplier service for vendor management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    /**
     * Create a new supplier.
     */
    @Transactional
    public SupplierDto.Response createSupplier(SupplierDto.CreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating supplier: {} for tenant: {}", request.getName(), tenantId);

        // Check for duplicate supplier code
        if (request.getSupplierCode() != null &&
                supplierRepository.existsBySupplierCodeAndTenantIdAndDeletedFalse(request.getSupplierCode(),
                        tenantId)) {
            throw new BusinessException("DUPLICATE_CODE", "Supplier code already exists");
        }

        Supplier supplier = Supplier.builder()
                .supplierCode(request.getSupplierCode())
                .name(request.getName())
                .contactPerson(request.getContactPerson())
                .email(request.getEmail())
                .phone(request.getPhone())
                .website(request.getWebsite())
                .addressLine1(request.getAddressLine1())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .taxId(request.getTaxId())
                .paymentTerms(request.getPaymentTerms())
                .creditLimit(request.getCreditLimit())
                .notes(request.getNotes())
                .active(true)
                .build();

        supplier.setTenantId(tenantId);
        supplier = supplierRepository.save(supplier);

        log.info("Supplier created: {}", supplier.getId());
        return mapToResponse(supplier);
    }

    /**
     * Get supplier by ID.
     */
    @Transactional(readOnly = true)
    public SupplierDto.Response getSupplier(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();

        Supplier supplier = supplierRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id.toString()));

        return mapToResponse(supplier);
    }

    /**
     * Get all suppliers with pagination.
     */
    @Transactional(readOnly = true)
    public Page<SupplierDto.Response> getSuppliers(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return supplierRepository.findByTenant(tenantId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get active suppliers (for dropdowns).
     */
    @Transactional(readOnly = true)
    public List<SupplierDto.Response> getActiveSuppliers() {
        String tenantId = TenantContext.getCurrentTenant();
        return supplierRepository.findActiveByTenant(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search suppliers.
     */
    @Transactional(readOnly = true)
    public Page<SupplierDto.Response> searchSuppliers(String query, Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return supplierRepository.search(tenantId, query, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Update supplier.
     */
    @Transactional
    public SupplierDto.Response updateSupplier(UUID id, SupplierDto.UpdateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Updating supplier: {} for tenant: {}", id, tenantId);

        Supplier supplier = supplierRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id.toString()));

        if (request.getSupplierCode() != null)
            supplier.setSupplierCode(request.getSupplierCode());
        if (request.getName() != null)
            supplier.setName(request.getName());
        if (request.getContactPerson() != null)
            supplier.setContactPerson(request.getContactPerson());
        if (request.getEmail() != null)
            supplier.setEmail(request.getEmail());
        if (request.getPhone() != null)
            supplier.setPhone(request.getPhone());
        if (request.getAltPhone() != null)
            supplier.setAltPhone(request.getAltPhone());
        if (request.getWebsite() != null)
            supplier.setWebsite(request.getWebsite());
        if (request.getAddressLine1() != null)
            supplier.setAddressLine1(request.getAddressLine1());
        if (request.getAddressLine2() != null)
            supplier.setAddressLine2(request.getAddressLine2());
        if (request.getCity() != null)
            supplier.setCity(request.getCity());
        if (request.getState() != null)
            supplier.setState(request.getState());
        if (request.getPostalCode() != null)
            supplier.setPostalCode(request.getPostalCode());
        if (request.getCountry() != null)
            supplier.setCountry(request.getCountry());
        if (request.getTaxId() != null)
            supplier.setTaxId(request.getTaxId());
        if (request.getPaymentTerms() != null)
            supplier.setPaymentTerms(request.getPaymentTerms());
        if (request.getCreditLimit() != null)
            supplier.setCreditLimit(request.getCreditLimit());
        if (request.getBankName() != null)
            supplier.setBankName(request.getBankName());
        if (request.getBankAccount() != null)
            supplier.setBankAccount(request.getBankAccount());
        if (request.getNotes() != null)
            supplier.setNotes(request.getNotes());
        if (request.getActive() != null)
            supplier.setActive(request.getActive());

        supplier = supplierRepository.save(supplier);
        log.info("Supplier updated: {}", supplier.getId());

        return mapToResponse(supplier);
    }

    /**
     * Delete supplier (soft delete).
     */
    @Transactional
    public void deleteSupplier(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Deleting supplier: {} for tenant: {}", id, tenantId);

        Supplier supplier = supplierRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id.toString()));

        supplier.softDelete();
        supplierRepository.save(supplier);

        log.info("Supplier soft deleted: {}", id);
    }

    private SupplierDto.Response mapToResponse(Supplier supplier) {
        return SupplierDto.Response.builder()
                .id(supplier.getId().toString())
                .supplierCode(supplier.getSupplierCode())
                .name(supplier.getName())
                .contactPerson(supplier.getContactPerson())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .altPhone(supplier.getAltPhone())
                .website(supplier.getWebsite())
                .addressLine1(supplier.getAddressLine1())
                .addressLine2(supplier.getAddressLine2())
                .city(supplier.getCity())
                .state(supplier.getState())
                .postalCode(supplier.getPostalCode())
                .country(supplier.getCountry())
                .fullAddress(supplier.getFullAddress())
                .taxId(supplier.getTaxId())
                .paymentTerms(supplier.getPaymentTerms())
                .creditLimit(supplier.getCreditLimit())
                .outstandingBalance(supplier.getOutstandingBalance())
                .bankName(supplier.getBankName())
                .notes(supplier.getNotes())
                .active(supplier.isActive())
                .createdAt(CommonUtils.formatDateTime(supplier.getCreatedAt()))
                .updatedAt(CommonUtils.formatDateTime(supplier.getUpdatedAt()))
                .build();
    }
}
