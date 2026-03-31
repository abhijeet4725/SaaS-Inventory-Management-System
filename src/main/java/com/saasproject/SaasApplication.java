package com.saasproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for SaaS Inventory + Billing + POS system.
 * 
 * Features enabled:
 * - JPA Auditing for automatic timestamps
 * - MongoDB Auditing for audit logs
 * - Caching with Redis
 * - Async processing
 * - Scheduled background jobs
 */
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableMongoAuditing
@EnableCaching
@EnableAsync
@EnableScheduling
public class SaasApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaasApplication.class, args);
    }
}
