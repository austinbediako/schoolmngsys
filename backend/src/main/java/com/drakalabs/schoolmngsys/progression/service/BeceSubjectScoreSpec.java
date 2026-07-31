package com.drakalabs.schoolmngsys.progression.service;

import java.util.UUID;

public record BeceSubjectScoreSpec(
        UUID subjectId,
        int grade
) {
}
