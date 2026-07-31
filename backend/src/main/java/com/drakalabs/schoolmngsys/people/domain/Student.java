package com.drakalabs.schoolmngsys.people.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * BR-EN-002: {@code studentNumber} is immutable once assigned — no setter, ever. Admissions
 * (APPLICANT status) is post-MVP; MVP creates students directly as ACTIVE (FR-STU-01 note).
 */
@Entity
@Table(name = "students")
public class Student extends BaseEntity {

    @Column(name = "student_number", nullable = false, updatable = false)
    private String studentNumber;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "other_names")
    private String otherNames;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "photo_path")
    private String photoPath;

    @Column(name = "admission_date", nullable = false)
    private LocalDate admissionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StudentStatus status = StudentStatus.ACTIVE;

    protected Student() {
    }

    public Student(
            String studentNumber,
            String firstName,
            String lastName,
            String otherNames,
            LocalDate dateOfBirth,
            Gender gender,
            LocalDate admissionDate) {
        this.studentNumber = studentNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.otherNames = otherNames;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.admissionDate = admissionDate;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getOtherNames() {
        return otherNames;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public LocalDate getAdmissionDate() {
        return admissionDate;
    }

    public StudentStatus getStatus() {
        return status;
    }

    public void updateBio(String firstName, String lastName, String otherNames) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.otherNames = otherNames;
    }

    /** BR-EN-005: called by {@code enrollment}'s exit workflow, which owns the reason/date. */
    public void transferOut() {
        this.status = StudentStatus.TRANSFERRED_OUT;
    }

    /** BR-EN-005: called by {@code enrollment}'s exit workflow, which owns the reason/date. */
    public void withdraw() {
        this.status = StudentStatus.WITHDRAWN;
    }
}
