package com.shopsphere.shopsphere.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration to handle redirections from old endpoints to new ones
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirect Admin endpoints to Dashboard endpoints
        registry.addRedirectViewController("/api/admin/dashboard", "/api/dashboard");
        registry.addRedirectViewController("/api/admin/products/top-selling", "/api/dashboard/products/top-selling");
        registry.addRedirectViewController("/api/admin/categories/distribution", "/api/dashboard/categories/distribution");
        registry.addRedirectViewController("/api/admin/revenue", "/api/dashboard/revenue");
        registry.addRedirectViewController("/api/admin/users/growth", "/api/dashboard/users/growth");
        registry.addRedirectViewController("/api/admin/orders/stats", "/api/dashboard/orders/stats");
        
        // Redirect Co-Worker endpoints to Dashboard endpoints
        registry.addRedirectViewController("/api/co-worker/dashboard", "/api/dashboard");
        registry.addRedirectViewController("/api/co-worker/products/top-selling", "/api/dashboard/products/top-selling");
        registry.addRedirectViewController("/api/co-worker/categories/distribution", "/api/dashboard/categories/distribution");
        registry.addRedirectViewController("/api/co-worker/orders/stats", "/api/dashboard/orders/stats");
        registry.addRedirectViewController("/api/co-worker/orders/scan-qr", "/api/dashboard/orders/scan-qr");
    }
} 