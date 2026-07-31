package com.drakalabs.schoolmngsys.progression.api;

import com.drakalabs.schoolmngsys.progression.service.BeceResultView;
import java.util.UUID;

public record BeceResultResponse(
        UUID id,
        UUID beceRegistrationId,
        UUID subjectId,
        int grade
) {

    public static BeceResultResponse from(BeceResultView view) {
        return new BeceResultResponse(
                view.id(),
                view.beceRegistrationId(),
                view.subjectId(),
                view.grade()
        );
    }
}
