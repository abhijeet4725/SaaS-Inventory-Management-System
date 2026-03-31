package com.saasproject.config;

import com.saasproject.tenant.context.TenantContext;
import com.saasproject.tenant.interceptor.TenantInterceptor;
import lombok.RequiredArgsConstructor;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Multi-tenant configuration using DISCRIMINATOR (shared database with
 * tenant_id).
 * 
 * Tenant identification sources (in order of priority):
 * 1. JWT token claim (tenantId)
 * 2. X-Tenant-ID request header
 * 3. Default tenant from configuration
 * 
 * All entities should extend BaseEntity which includes tenantId field.
 * Repositories should use @Query annotations with tenant filtering or
 * extend TenantAwareRepository.
 */
@Configuration
@RequiredArgsConstructor
public class MultiTenantConfig implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;

    @Value("${app.tenant.default-tenant:default}")
    private String defaultTenant;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/v1/**")
                .excludePathPatterns("/v1/auth/**");
    }

    @Bean
    public CurrentTenantIdentifierResolver<String> currentTenantIdentifierResolver() {
        return new CurrentTenantIdentifierResolver<>() {
            @Override
            public String resolveCurrentTenantIdentifier() {
                String tenantId = TenantContext.getCurrentTenant();
                return tenantId != null ? tenantId : defaultTenant;
            }

            @Override
            public boolean validateExistingCurrentSessions() {
                return true;
            }
        };
    }

    @Bean
    public MultiTenantConnectionProvider<String> multiTenantConnectionProvider(DataSource dataSource) {
        return new MultiTenantConnectionProvider<>() {
            @Override
            public Connection getAnyConnection() throws SQLException {
                return dataSource.getConnection();
            }

            @Override
            public void releaseAnyConnection(Connection connection) throws SQLException {
                connection.close();
            }

            @Override
            public Connection getConnection(String tenantIdentifier) throws SQLException {
                // For DISCRIMINATOR strategy, we use the same connection
                // and filter by tenant_id in queries
                Connection connection = getAnyConnection();

                // Optionally set tenant context at database level
                // This is useful for Row Level Security in PostgreSQL:
                // connection.createStatement().execute(
                // "SET app.current_tenant = '" + tenantIdentifier + "'"
                // );

                return connection;
            }

            @Override
            public void releaseConnection(String tenantIdentifier, Connection connection)
                    throws SQLException {
                releaseAnyConnection(connection);
            }

            @Override
            public boolean supportsAggressiveRelease() {
                return true;
            }

            @Override
            public boolean isUnwrappableAs(Class<?> unwrapType) {
                return false;
            }

            @Override
            public <T> T unwrap(Class<T> unwrapType) {
                throw new UnsupportedOperationException("Unwrap not supported");
            }
        };
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(
            MultiTenantConnectionProvider<String> multiTenantConnectionProvider,
            CurrentTenantIdentifierResolver<String> currentTenantIdentifierResolver) {
        return (Map<String, Object> hibernateProperties) -> {
            hibernateProperties.put(
                    AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER,
                    multiTenantConnectionProvider);
            hibernateProperties.put(
                    AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER,
                    currentTenantIdentifierResolver);
        };
    }
}
