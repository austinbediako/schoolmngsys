package com.drakalabs.schoolmngsys.analytics.api;

import com.drakalabs.schoolmngsys.analytics.service.DashboardQueryService;
import com.drakalabs.schoolmngsys.analytics.service.HeadDashboardView;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardQueryService dashboardQueryService;

    public DashboardController(DashboardQueryService dashboardQueryService) {
        this.dashboardQueryService = dashboardQueryService;
    }

    @GetMapping("/head")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW_SCHOOL')")
    public HeadDashboardResponse getHeadDashboard() {
        HeadDashboardView view = dashboardQueryService.getHeadDashboard();
        return HeadDashboardResponse.from(view);
    }
}
