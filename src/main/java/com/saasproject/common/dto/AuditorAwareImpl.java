package com.saasproject.common.dto;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Auditor provider for JPA auditing.
 * 
 * Extracts the current user from the security context for
 * automatic population of createdBy and updatedBy fields.
 * 
 * Note: Registered as a bean in DataConfig, not as @Component.
 */
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.of("SYSTEM");
        }

        return Optional.of(authentication.getName());
    }
}
