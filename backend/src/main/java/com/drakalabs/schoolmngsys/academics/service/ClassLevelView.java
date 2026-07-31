package com.drakalabs.schoolmngsys.academics.service;

import com.drakalabs.schoolmngsys.academics.domain.ClassLevel;
import java.util.UUID;

public record ClassLevelView(
        UUID id,
        String code,
        String canonicalName,
        String basicAlias,
        int sequence,
        UUID departmentId
) {

    public static ClassLevelView from(ClassLevel level) {
        return new ClassLevelView(
                level.getId(),
                level.getCode(),
                level.getCanonicalName(),
                level.getBasicAlias(),
                level.getSequence(),
                level.getDepartment().getId()
        );
    }
}
