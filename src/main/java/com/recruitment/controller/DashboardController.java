package com.recruitment.controller;

import com.recruitment.dto.DashboardStatsResponse;
import com.recruitment.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/api/admin/dashboard/stats")
    public DashboardStatsResponse adminStats() {
        return dashboardService.getStats();
    }

    @GetMapping("/api/recruiter/dashboard/stats")
    public DashboardStatsResponse recruiterStats() {
        return dashboardService.getStats();
    }
}
