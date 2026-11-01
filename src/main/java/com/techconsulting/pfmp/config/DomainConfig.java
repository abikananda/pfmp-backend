package com.techconsulting.pfmp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;


@Slf4j
@Configuration
@EntityScan("com.techconsulting.pfmp.domain")
@EnableJpaRepositories("com.techconsulting.pfmp.repository")
@EnableTransactionManagement
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider", auditorAwareRef = "auditorProvider")
public class DomainConfig {

    // Use Clock bean for better testing and predictability
    @Bean
    public Clock clock() {
        log.debug("Initializing Clock bean");
        return Clock.systemUTC();
    }

    @Bean(name = "auditingDateTimeProvider")
    public DateTimeProvider dateTimeProvider(Clock clock) {
        log.debug("Initializing DateTimeProvider bean");
        return () -> Optional.of(OffsetDateTime.now(clock));
    }

    @Bean(name = "auditorProvider")
    public AuditorAware<String> auditorProvider() {
        log.debug("Initializing AuditorAware bean");
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
                log.debug("Using System as auditor");
                return Optional.of("System");
            }
            String auditorName = authentication.getName();
            log.debug("Using {} as auditor", auditorName);
            return Optional.ofNullable(auditorName);
        };
    }
}
