package com.drakalabs.schoolmngsys.people.service;

import com.drakalabs.schoolmngsys.people.domain.StaffQualification;
import java.util.UUID;

public record StaffQualificationView(UUID id, String qualification, String institution, Integer yearObtained) {

    public static StaffQualificationView from(StaffQualification qualification) {
        return new StaffQualificationView(
                qualification.getId(), qualification.getQualification(), qualification.getInstitution(), qualification.getYearObtained());
    }
}
