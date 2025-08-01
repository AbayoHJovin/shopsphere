package com.shopsphere.shopsphere.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * This controller redirects co-worker requests to the unified DashboardController
 * @deprecated Use DashboardController instead
 */
@Controller
@RequestMapping("/api/co-worker")
public class CoWorkerController {
    // This class is now just a redirect to the unified DashboardController
    // All functionality has been moved to DashboardController
}