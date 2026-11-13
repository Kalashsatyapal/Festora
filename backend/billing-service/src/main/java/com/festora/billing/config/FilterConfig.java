package com.festora.billing.config;

import com.festora.billing.security.InternalApiKeyFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<InternalApiKeyFilter> internalApiKeyFilterRegistration(
            InternalApiKeyFilter filter
    ) {
        FilterRegistrationBean<InternalApiKeyFilter> registration =
                new FilterRegistrationBean<>();

        registration.setFilter(filter);
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        return registration;
    }
}