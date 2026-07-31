package com.drakalabs.schoolmngsys.academics.service;

import com.drakalabs.schoolmngsys.academics.domain.SchoolClass;
import java.util.UUID;

public record ClassView(UUID id, UUID classLevelId, String classLevelCode, String classLevelName, String stream, int capacity) {

    public static ClassView from(SchoolClass schoolClass) {
        return new ClassView(
                schoolClass.getId(),
                schoolClass.getClassLevel().getId(),
                schoolClass.getClassLevel().getCode(),
                schoolClass.getClassLevel().getCanonicalName(),
                schoolClass.getStream(),
                schoolClass.getCapacity());
    }

    public String name() {
        return classLevelCode + stream;
    }
}
