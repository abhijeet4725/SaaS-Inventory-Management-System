package com.saasproject.config;

import com.saasproject.common.dto.AuditorAwareImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Data configuration for JPA and MongoDB repositories.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.saasproject.modules")
@EnableMongoRepositories(basePackages = "com.saasproject.modules.audit")
public class DataConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return new AuditorAwareImpl();
    }
}
