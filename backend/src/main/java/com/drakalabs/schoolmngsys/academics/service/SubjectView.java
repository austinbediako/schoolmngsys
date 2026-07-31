package com.drakalabs.schoolmngsys.academics.service;

import com.drakalabs.schoolmngsys.academics.domain.Subject;
import java.util.UUID;

public record SubjectView(UUID id, String name, String code) {

    public static SubjectView from(Subject subject) {
        return new SubjectView(subject.getId(), subject.getName(), subject.getCode());
    }
}
