package com.drakalabs.schoolmngsys.people.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** BR-ST-001: teaching staff need these on file before subject assignment (process control, WP-2). */
@Entity
@Table(name = "staff_qualifications")
public class StaffQualification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(name = "qualification", nullable = false)
    private String qualification;

    @Column(name = "institution")
    private String institution;

    @Column(name = "year_obtained")
    private Integer yearObtained;

    protected StaffQualification() {
    }

    public StaffQualification(Staff staff, String qualification, String institution, Integer yearObtained) {
        this.staff = staff;
        this.qualification = qualification;
        this.institution = institution;
        this.yearObtained = yearObtained;
    }

    public Staff getStaff() {
        return staff;
    }

    public String getQualification() {
        return qualification;
    }

    public String getInstitution() {
        return institution;
    }

    public Integer getYearObtained() {
        return yearObtained;
    }
}
