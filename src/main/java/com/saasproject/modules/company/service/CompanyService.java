package com.saasproject.modules.company.service;

import com.saasproject.common.exceptions.ResourceNotFoundException;
import com.saasproject.common.utils.CommonUtils;
import com.saasproject.modules.company.dto.CompanyDto;
import com.saasproject.modules.company.entity.Company;
import com.saasproject.modules.company.repository.CompanyRepository;
import com.saasproject.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Company service for managing tenant company details.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    /**
     * Get current company for the authenticated tenant.
     */
    @Transactional(readOnly = true)
    public CompanyDto.Response getCurrentCompany() {
        String tenantId = TenantContext.getCurrentTenant();

        Company company = companyRepository.findByTenantIdAndDeletedFalse(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "tenantId", tenantId));

        return mapToResponse(company);
    }

    /**
     * Create a new company (during tenant onboarding).
     */
    @Transactional
    public CompanyDto.Response createCompany(String tenantId, CompanyDto.CreateRequest request) {
        log.info("Creating company for tenant: {}", tenantId);

        Company company = Company.builder()
                .name(request.getName())
                .legalName(request.getLegalName())
                .registrationNumber(request.getRegistrationNumber())
                .taxId(request.getTaxId())
                .email(request.getEmail())
                .phone(request.getPhone())
                .website(request.getWebsite())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .active(true)
                .build();

        company.setTenantId(tenantId);
        company = companyRepository.save(company);

        log.info("Company created: {} for tenant: {}", company.getId(), tenantId);
        return mapToResponse(company);
    }

    /**
     * Update company details.
     */
    @Transactional
    public CompanyDto.Response updateCompany(CompanyDto.UpdateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Updating company for tenant: {}", tenantId);

        Company company = companyRepository.findByTenantIdAndDeletedFalse(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "tenantId", tenantId));

        if (request.getName() != null)
            company.setName(request.getName());
        if (request.getLegalName() != null)
            company.setLegalName(request.getLegalName());
        if (request.getRegistrationNumber() != null)
            company.setRegistrationNumber(request.getRegistrationNumber());
        if (request.getTaxId() != null)
            company.setTaxId(request.getTaxId());
        if (request.getEmail() != null)
            company.setEmail(request.getEmail());
        if (request.getPhone() != null)
            company.setPhone(request.getPhone());
        if (request.getWebsite() != null)
            company.setWebsite(request.getWebsite());
        if (request.getAddressLine1() != null)
            company.setAddressLine1(request.getAddressLine1());
        if (request.getAddressLine2() != null)
            company.setAddressLine2(request.getAddressLine2());
        if (request.getCity() != null)
            company.setCity(request.getCity());
        if (request.getState() != null)
            company.setState(request.getState());
        if (request.getPostalCode() != null)
            company.setPostalCode(request.getPostalCode());
        if (request.getCountry() != null)
            company.setCountry(request.getCountry());
        if (request.getLogoUrl() != null)
            company.setLogoUrl(request.getLogoUrl());

        company = companyRepository.save(company);
        log.info("Company updated: {}", company.getId());

        return mapToResponse(company);
    }

    /**
     * Update company settings.
     */
    @Transactional
    public CompanyDto.Response updateSettings(CompanyDto.SettingsRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Updating settings for tenant: {}", tenantId);

        Company company = companyRepository.findByTenantIdAndDeletedFalse(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "tenantId", tenantId));

        if (request.getCurrency() != null)
            company.setCurrency(request.getCurrency());
        if (request.getTimezone() != null)
            company.setTimezone(request.getTimezone());
        if (request.getDateFormat() != null)
            company.setDateFormat(request.getDateFormat());
        if (request.getDefaultTaxRate() != null)
            company.setDefaultTaxRate(request.getDefaultTaxRate());
        if (request.getInvoicePrefix() != null)
            company.setInvoicePrefix(request.getInvoicePrefix());
        if (request.getInvoiceFooter() != null)
            company.setInvoiceFooter(request.getInvoiceFooter());

        company = companyRepository.save(company);
        log.info("Settings updated for tenant: {}", tenantId);

        return mapToResponse(company);
    }

    private CompanyDto.Response mapToResponse(Company company) {
        return CompanyDto.Response.builder()
                .id(company.getId().toString())
                .tenantId(company.getTenantId())
                .name(company.getName())
                .legalName(company.getLegalName())
                .registrationNumber(company.getRegistrationNumber())
                .taxId(company.getTaxId())
                .email(company.getEmail())
                .phone(company.getPhone())
                .website(company.getWebsite())
                .logoUrl(company.getLogoUrl())
                .addressLine1(company.getAddressLine1())
                .addressLine2(company.getAddressLine2())
                .city(company.getCity())
                .state(company.getState())
                .postalCode(company.getPostalCode())
                .country(company.getCountry())
                .fullAddress(company.getFullAddress())
                .currency(company.getCurrency())
                .timezone(company.getTimezone())
                .dateFormat(company.getDateFormat())
                .defaultTaxRate(company.getDefaultTaxRate())
                .invoicePrefix(company.getInvoicePrefix())
                .invoiceFooter(company.getInvoiceFooter())
                .subscriptionTier(company.getSubscriptionTier().name())
                .active(company.isActive())
                .createdAt(CommonUtils.formatDateTime(company.getCreatedAt()))
                .updatedAt(CommonUtils.formatDateTime(company.getUpdatedAt()))
                .build();
    }
}
