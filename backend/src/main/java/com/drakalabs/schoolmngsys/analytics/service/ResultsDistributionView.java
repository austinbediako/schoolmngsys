package com.drakalabs.schoolmngsys.analytics.service;

import java.util.Map;

public record ResultsDistributionView(
        long totalResultsCount,
        Map<String, Long> gradeDistribution
) {
}
