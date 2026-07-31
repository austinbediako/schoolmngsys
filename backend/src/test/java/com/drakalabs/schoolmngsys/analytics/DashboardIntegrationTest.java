package com.drakalabs.schoolmngsys.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import com.drakalabs.schoolmngsys.AbstractIntegrationTest;
import com.drakalabs.schoolmngsys.academics.domain.AcademicYear;
import com.drakalabs.schoolmngsys.academics.domain.AcademicYearStatus;
import com.drakalabs.schoolmngsys.academics.repository.AcademicYearRepository;
import com.drakalabs.schoolmngsys.analytics.service.DashboardQueryService;
import com.drakalabs.schoolmngsys.analytics.service.HeadDashboardView;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DashboardIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AcademicYearRepository academicYearRepository;

    @Autowired
    private DashboardQueryService dashboardQueryService;

    @Test
    void returnsEmptyMetricsWhenNoActiveAcademicYearExists() {
        HeadDashboardView dashboard = dashboardQueryService.getHeadDashboard();
        assertThat(dashboard).isNotNull();
    }

    @Test
    void aggregatesHeadDashboardMetricsForActiveAcademicYear() {
        AcademicYear activeYear = academicYearRepository.findByStatusAndArchivedAtIsNull(AcademicYearStatus.ACTIVE)
                .orElseGet(() -> {
                    AcademicYear year = new AcademicYear("2026/2027-ANALYTICS", LocalDate.of(2026, 9, 1), LocalDate.of(2027, 7, 31));
                    year.activate();
                    return academicYearRepository.save(year);
                });

        HeadDashboardView dashboard = dashboardQueryService.getHeadDashboard();
        assertThat(dashboard).isNotNull();
        assertThat(dashboard.activeAcademicYearId()).isEqualTo(activeYear.getId());
        assertThat(dashboard.enrollment()).isNotNull();
        assertThat(dashboard.attendance()).isNotNull();
        assertThat(dashboard.finance()).isNotNull();
        assertThat(dashboard.resultsDistribution()).isNotNull();
    }
}
