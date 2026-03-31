package com.saasproject.modules.customer.service;

import com.saasproject.common.exceptions.ResourceNotFoundException;
import com.saasproject.common.utils.CommonUtils;
import com.saasproject.modules.customer.dto.CustomerDto;
import com.saasproject.modules.customer.entity.Customer;
import com.saasproject.modules.customer.repository.CustomerRepository;
import com.saasproject.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Customer service for customer management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * Create a new customer.
     */
    @Transactional
    public CustomerDto.Response createCustomer(CustomerDto.CreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating customer: {} for tenant: {}", request.getName(), tenantId);

        Customer customer = Customer.builder()
                .customerCode(CommonUtils.generateNumber("CUS"))
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .customerType(parseCustomerType(request.getCustomerType()))
                .companyName(request.getCompanyName())
                .taxId(request.getTaxId())
                .addressLine1(request.getAddressLine1())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .creditLimit(request.getCreditLimit())
                .notes(request.getNotes())
                .active(true)
                .build();

        customer.setTenantId(tenantId);
        customer = customerRepository.save(customer);

        log.info("Customer created: {}", customer.getId());
        return mapToResponse(customer);
    }

    /**
     * Get customer by ID.
     */
    @Transactional(readOnly = true)
    public CustomerDto.Response getCustomer(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();

        Customer customer = customerRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id.toString()));

        return mapToResponse(customer);
    }

    /**
     * Get customers with pagination.
     */
    @Transactional(readOnly = true)
    public Page<CustomerDto.Response> getCustomers(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return customerRepository.findByTenant(tenantId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Search customers.
     */
    @Transactional(readOnly = true)
    public Page<CustomerDto.Response> searchCustomers(String query, Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return customerRepository.search(tenantId, query, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Find customer by phone.
     */
    @Transactional(readOnly = true)
    public CustomerDto.Response findByPhone(String phone) {
        String tenantId = TenantContext.getCurrentTenant();

        Customer customer = customerRepository.findByPhoneAndTenant(phone, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "phone", phone));

        return mapToResponse(customer);
    }

    /**
     * Update customer.
     */
    @Transactional
    public CustomerDto.Response updateCustomer(UUID id, CustomerDto.UpdateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Updating customer: {}", id);

        Customer customer = customerRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id.toString()));

        if (request.getName() != null)
            customer.setName(request.getName());
        if (request.getEmail() != null)
            customer.setEmail(request.getEmail());
        if (request.getPhone() != null)
            customer.setPhone(request.getPhone());
        if (request.getAltPhone() != null)
            customer.setAltPhone(request.getAltPhone());
        if (request.getCustomerType() != null)
            customer.setCustomerType(parseCustomerType(request.getCustomerType()));
        if (request.getCompanyName() != null)
            customer.setCompanyName(request.getCompanyName());
        if (request.getTaxId() != null)
            customer.setTaxId(request.getTaxId());
        if (request.getAddressLine1() != null)
            customer.setAddressLine1(request.getAddressLine1());
        if (request.getAddressLine2() != null)
            customer.setAddressLine2(request.getAddressLine2());
        if (request.getCity() != null)
            customer.setCity(request.getCity());
        if (request.getState() != null)
            customer.setState(request.getState());
        if (request.getPostalCode() != null)
            customer.setPostalCode(request.getPostalCode());
        if (request.getCountry() != null)
            customer.setCountry(request.getCountry());
        if (request.getCreditLimit() != null)
            customer.setCreditLimit(request.getCreditLimit());
        if (request.getNotes() != null)
            customer.setNotes(request.getNotes());
        if (request.getActive() != null)
            customer.setActive(request.getActive());

        customer = customerRepository.save(customer);
        log.info("Customer updated: {}", customer.getId());

        return mapToResponse(customer);
    }

    /**
     * Delete customer (soft delete).
     */
    @Transactional
    public void deleteCustomer(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Deleting customer: {}", id);

        Customer customer = customerRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id.toString()));

        customer.softDelete();
        customerRepository.save(customer);

        log.info("Customer soft deleted: {}", id);
    }

    private Customer.CustomerType parseCustomerType(String type) {
        if (type == null)
            return Customer.CustomerType.INDIVIDUAL;
        try {
            return Customer.CustomerType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Customer.CustomerType.INDIVIDUAL;
        }
    }

    private CustomerDto.Response mapToResponse(Customer customer) {
        return CustomerDto.Response.builder()
                .id(customer.getId().toString())
                .customerCode(customer.getCustomerCode())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .altPhone(customer.getAltPhone())
                .customerType(customer.getCustomerType().name())
                .companyName(customer.getCompanyName())
                .taxId(customer.getTaxId())
                .addressLine1(customer.getAddressLine1())
                .addressLine2(customer.getAddressLine2())
                .city(customer.getCity())
                .state(customer.getState())
                .postalCode(customer.getPostalCode())
                .country(customer.getCountry())
                .fullAddress(customer.getFullAddress())
                .creditLimit(customer.getCreditLimit())
                .outstandingBalance(customer.getOutstandingBalance())
                .totalPurchases(customer.getTotalPurchases())
                .loyaltyPoints(customer.getLoyaltyPoints())
                .notes(customer.getNotes())
                .active(customer.isActive())
                .createdAt(CommonUtils.formatDateTime(customer.getCreatedAt()))
                .build();
    }
}
