package com.drakalabs.schoolmngsys.progression.service;

import com.drakalabs.schoolmngsys.progression.domain.BeceResult;
import java.util.UUID;

public record BeceResultView(
        UUID id,
        UUID beceRegistrationId,
        UUID subjectId,
        int grade
) {

    public static BeceResultView from(BeceResult r) {
        return new BeceResultView(
                r.getId(),
                r.getBeceRegistrationId(),
                r.getSubjectId(),
                r.getGrade()
        );
    }
}
