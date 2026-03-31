package com.saasproject.modules.company.repository;

import com.saasproject.modules.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Company repository.
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {

    Optional<Company> findByTenantIdAndDeletedFalse(String tenantId);

    boolean existsByTenantIdAndDeletedFalse(String tenantId);
}
