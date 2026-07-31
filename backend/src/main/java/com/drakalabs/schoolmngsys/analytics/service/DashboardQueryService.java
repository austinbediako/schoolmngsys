package com.drakalabs.schoolmngsys.analytics.service;

import com.drakalabs.schoolmngsys.academics.service.AcademicYearQueryService;
import com.drakalabs.schoolmngsys.academics.service.AcademicYearView;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardQueryService {

    private final AcademicYearQueryService academicYearQueryService;
    private final JdbcTemplate jdbcTemplate;

    public DashboardQueryService(AcademicYearQueryService academicYearQueryService, JdbcTemplate jdbcTemplate) {
        this.academicYearQueryService = academicYearQueryService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public HeadDashboardView getHeadDashboard() {
        Optional<AcademicYearView> activeYearOpt = academicYearQueryService.findActiveYear();

        if (activeYearOpt.isEmpty()) {
            return new HeadDashboardView(
                    null,
                    "None",
                    new EnrollmentAnalyticsView(0, 0, 0, 0, Map.of()),
                    new AttendanceAnalyticsView(0, 0, 0, 0, 0, BigDecimal.ZERO),
                    new FinanceAnalyticsView(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                    new ResultsDistributionView(0, Map.of())
            );
        }

        AcademicYearView activeYear = activeYearOpt.get();
        UUID yearId = activeYear.id();

        EnrollmentAnalyticsView enrollmentView = buildEnrollmentAnalytics(yearId);
        AttendanceAnalyticsView attendanceView = buildAttendanceAnalytics(yearId);
        FinanceAnalyticsView financeView = buildFinanceAnalytics(yearId);
        ResultsDistributionView resultsView = buildResultsDistribution(yearId);

        return new HeadDashboardView(
                yearId,
                activeYear.label(),
                enrollmentView,
                attendanceView,
                financeView,
                resultsView
        );
    }

    private EnrollmentAnalyticsView buildEnrollmentAnalytics(UUID yearId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT class_level_name, gender, enrollment_count FROM v_enrollment_analytics WHERE academic_year_id = ?",
                yearId
        );

        long total = 0;
        long male = 0;
        long female = 0;
        long unknown = 0;
        Map<String, Long> countByLevel = new HashMap<>();

        for (Map<String, Object> row : rows) {
            String levelName = (String) row.get("class_level_name");
            String gender = (String) row.get("gender");
            long count = ((Number) row.get("enrollment_count")).longValue();

            total += count;
            if ("MALE".equalsIgnoreCase(gender)) {
                male += count;
            } else if ("FEMALE".equalsIgnoreCase(gender)) {
                female += count;
            } else {
                unknown += count;
            }

            countByLevel.merge(levelName, count, Long::sum);
        }

        return new EnrollmentAnalyticsView(total, male, female, unknown, countByLevel);
    }

    private AttendanceAnalyticsView buildAttendanceAnalytics(UUID yearId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT status, record_count FROM v_attendance_analytics WHERE academic_year_id = ?",
                yearId
        );

        long total = 0;
        long present = 0;
        long absent = 0;
        long late = 0;
        long excused = 0;

        for (Map<String, Object> row : rows) {
            String status = (String) row.get("status");
            long count = ((Number) row.get("record_count")).longValue();
            total += count;

            if ("PRESENT".equalsIgnoreCase(status)) present += count;
            else if ("ABSENT".equalsIgnoreCase(status)) absent += count;
            else if ("LATE".equalsIgnoreCase(status)) late += count;
            else if ("EXCUSED".equalsIgnoreCase(status)) excused += count;
        }

        BigDecimal rate = BigDecimal.ZERO;
        if (total > 0) {
            long attended = present + late;
            rate = BigDecimal.valueOf(attended)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        }

        return new AttendanceAnalyticsView(total, present, absent, late, excused, rate);
    }

    private FinanceAnalyticsView buildFinanceAnalytics(UUID yearId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT total_invoiced, total_collected FROM v_finance_analytics WHERE academic_year_id = ?",
                yearId
        );

        BigDecimal invoiced = BigDecimal.ZERO;
        BigDecimal collected = BigDecimal.ZERO;

        if (!rows.isEmpty()) {
            Map<String, Object> row = rows.get(0);
            if (row.get("total_invoiced") != null) {
                invoiced = new BigDecimal(row.get("total_invoiced").toString());
            }
            if (row.get("total_collected") != null) {
                collected = new BigDecimal(row.get("total_collected").toString());
            }
        }

        BigDecimal arrears = invoiced.subtract(collected).max(BigDecimal.ZERO);
        BigDecimal collectionRate = BigDecimal.ZERO;
        if (invoiced.compareTo(BigDecimal.ZERO) > 0) {
            collectionRate = collected.multiply(BigDecimal.valueOf(100)).divide(invoiced, 2, RoundingMode.HALF_UP);
        }

        return new FinanceAnalyticsView(invoiced, collected, arrears, collectionRate);
    }

    private ResultsDistributionView buildResultsDistribution(UUID yearId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT tr.grade, COUNT(tr.id) AS count " +
                "FROM term_results tr " +
                "JOIN enrollments e ON tr.enrollment_id = e.id " +
                "WHERE e.academic_year_id = ? AND tr.archived_at IS NULL AND tr.status = 'PUBLISHED' " +
                "GROUP BY tr.grade",
                yearId
        );

        long total = 0;
        Map<String, Long> distribution = new HashMap<>();

        for (Map<String, Object> row : rows) {
            String grade = (String) row.get("grade");
            long count = ((Number) row.get("count")).longValue();
            total += count;
            if (grade != null) {
                distribution.put(grade, count);
            }
        }

        return new ResultsDistributionView(total, distribution);
    }
}
