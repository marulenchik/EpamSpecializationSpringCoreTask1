package com.gym.crm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.actuate.audit.InMemoryAuditEventRepository;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

/**
 * Configuration class for enhanced logging and monitoring features
 */
@Configuration
public class LoggingConfiguration {

    /**
     * Configures audit event repository for security and operational auditing
     */
    @Bean
    public AuditEventRepository auditEventRepository() {
        return new InMemoryAuditEventRepository(1000); // Keep last 1000 audit events
    }

    /**
     * Registers performance monitoring filter
     */
    @Bean
    public FilterRegistrationBean<PerformanceLoggingFilter> performanceLoggingFilter() {
        FilterRegistrationBean<PerformanceLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new PerformanceLoggingFilter());
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(2); // After transaction interceptor
        return registrationBean;
    }
}


