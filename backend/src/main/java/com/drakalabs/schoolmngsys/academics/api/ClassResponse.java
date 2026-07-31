package com.drakalabs.schoolmngsys.academics.api;

import com.drakalabs.schoolmngsys.academics.service.ClassView;
import java.util.UUID;

public record ClassResponse(UUID id, String classLevelCode, String classLevelName, String stream, int capacity) {

    public static ClassResponse from(ClassView view) {
        return new ClassResponse(view.id(), view.classLevelCode(), view.classLevelName(), view.stream(), view.capacity());
    }
}
