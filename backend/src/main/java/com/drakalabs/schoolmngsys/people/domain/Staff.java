package com.drakalabs.schoolmngsys.people.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * BR-ST-001: {@code staffNumber} is immutable once assigned. BR-ST-002: ending employment
 * preserves all historical records — this only flips status/end-date, nothing is deleted.
 */
@Entity
@Table(name = "staff")
public class Staff extends BaseEntity {

    @Column(name = "staff_number", nullable = false, updatable = false)
    private String staffNumber;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "staff_type", nullable = false)
    private StaffType staffType;

    @Column(name = "ges_registration_number")
    private String gesRegistrationNumber;

    @Column(name = "employment_start_date", nullable = false)
    private LocalDate employmentStartDate;

    @Column(name = "employment_end_date")
    private LocalDate employmentEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StaffStatus status = StaffStatus.ACTIVE;

    protected Staff() {
    }

    public Staff(
            String staffNumber,
            String firstName,
            String lastName,
            StaffType staffType,
            String gesRegistrationNumber,
            LocalDate employmentStartDate) {
        this.staffNumber = staffNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.staffType = staffType;
        this.gesRegistrationNumber = gesRegistrationNumber;
        this.employmentStartDate = employmentStartDate;
    }

    public String getStaffNumber() {
        return staffNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public StaffType getStaffType() {
        return staffType;
    }

    public String getGesRegistrationNumber() {
        return gesRegistrationNumber;
    }

    public LocalDate getEmploymentStartDate() {
        return employmentStartDate;
    }

    public LocalDate getEmploymentEndDate() {
        return employmentEndDate;
    }

    public StaffStatus getStatus() {
        return status;
    }

    public void updateBio(String firstName, String lastName, String gesRegistrationNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.gesRegistrationNumber = gesRegistrationNumber;
    }

    public void endEmployment(LocalDate endDate) {
        this.status = StaffStatus.ENDED;
        this.employmentEndDate = endDate;
    }
}
