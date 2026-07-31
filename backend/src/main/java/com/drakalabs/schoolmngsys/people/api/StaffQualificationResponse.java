package com.drakalabs.schoolmngsys.people.api;

import com.drakalabs.schoolmngsys.people.service.StaffQualificationView;
import java.util.UUID;

public record StaffQualificationResponse(UUID id, String qualification, String institution, Integer yearObtained) {

    public static StaffQualificationResponse from(StaffQualificationView view) {
        return new StaffQualificationResponse(view.id(), view.qualification(), view.institution(), view.yearObtained());
    }
}
