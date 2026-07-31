package com.drakalabs.schoolmngsys.progression.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "bece_results")
public class BeceResult extends BaseEntity {

    @Column(name = "bece_registration_id", nullable = false)
    private UUID beceRegistrationId;

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    @Column(name = "grade", nullable = false)
    private int grade;

    protected BeceResult() {
    }

    public BeceResult(UUID beceRegistrationId, UUID subjectId, int grade) {
        this.beceRegistrationId = beceRegistrationId;
        this.subjectId = subjectId;
        this.grade = grade;
    }

    public UUID getBeceRegistrationId() {
        return beceRegistrationId;
    }

    public UUID getSubjectId() {
        return subjectId;
    }

    public int getGrade() {
        return grade;
    }

    public void updateGrade(int grade) {
        this.grade = grade;
    }
}
