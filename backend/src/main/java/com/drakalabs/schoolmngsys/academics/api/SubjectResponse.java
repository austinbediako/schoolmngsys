package com.drakalabs.schoolmngsys.academics.api;

import com.drakalabs.schoolmngsys.academics.service.SubjectView;
import java.util.UUID;

public record SubjectResponse(UUID id, String name, String code) {

    public static SubjectResponse from(SubjectView view) {
        return new SubjectResponse(view.id(), view.name(), view.code());
    }
}
