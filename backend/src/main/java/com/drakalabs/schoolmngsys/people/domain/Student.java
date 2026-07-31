package com.drakalabs.schoolmngsys.people.domain;

import com.drakalabs.schoolmngsys.people.service.StudentAdmissionDetails;
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

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "previous_school")
    private String previousSchool;

    @Column(name = "residential_address")
    private String residentialAddress;

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;

    @Column(name = "emergency_contact_relationship")
    private String emergencyContactRelationship;

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

    public String getNationality() {
        return nationality;
    }

    public String getPreviousSchool() {
        return previousSchool;
    }

    public String getResidentialAddress() {
        return residentialAddress;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public String getEmergencyContactRelationship() {
        return emergencyContactRelationship;
    }

    public void updateBio(String firstName, String lastName, String otherNames) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.otherNames = otherNames;
    }

    public void updateAdmissionDetails(StudentAdmissionDetails details) {
        this.nationality = details.nationality();
        this.previousSchool = details.previousSchool();
        this.residentialAddress = details.residentialAddress();
        this.emergencyContactName = details.emergencyContactName();
        this.emergencyContactPhone = details.emergencyContactPhone();
        this.emergencyContactRelationship = details.emergencyContactRelationship();
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
