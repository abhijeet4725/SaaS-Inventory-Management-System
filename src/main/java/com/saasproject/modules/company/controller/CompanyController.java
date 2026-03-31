package com.saasproject.modules.company.controller;

import com.saasproject.common.api_response.ApiResponse;
import com.saasproject.modules.company.dto.CompanyDto;
import com.saasproject.modules.company.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Company controller for managing tenant company details.
 */
@RestController
@RequestMapping("/v1/company")
@RequiredArgsConstructor
@Tag(name = "Company", description = "Company/Tenant management")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    @Operation(summary = "Get company", description = "Get current company details")
    public ResponseEntity<ApiResponse<CompanyDto.Response>> getCurrentCompany() {
        CompanyDto.Response response = companyService.getCurrentCompany();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update company", description = "Update company details")
    public ResponseEntity<ApiResponse<CompanyDto.Response>> updateCompany(
            @Valid @RequestBody CompanyDto.UpdateRequest request) {

        CompanyDto.Response response = companyService.updateCompany(request);
        return ResponseEntity.ok(ApiResponse.success("Company updated", response));
    }

    @PutMapping("/settings")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update settings", description = "Update company settings (currency, tax, etc.)")
    public ResponseEntity<ApiResponse<CompanyDto.Response>> updateSettings(
            @Valid @RequestBody CompanyDto.SettingsRequest request) {

        CompanyDto.Response response = companyService.updateSettings(request);
        return ResponseEntity.ok(ApiResponse.success("Settings updated", response));
    }
}
