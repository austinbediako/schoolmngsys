package com.drakalabs.schoolmngsys.people.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** BR-EN-004: every ACTIVE student needs >= 1 of these, >= 1 flagged primary contact. */
@Entity
@Table(name = "student_guardians")
public class StudentGuardian extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guardian_id", nullable = false)
    private Guardian guardian;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false)
    private RelationshipType relationshipType;

    @Column(name = "is_primary_contact", nullable = false)
    private boolean primaryContact;

    @Column(name = "has_custody", nullable = false)
    private boolean hasCustody = true;

    @Column(name = "receives_billing", nullable = false)
    private boolean receivesBilling;

    @Column(name = "receives_academic_reports", nullable = false)
    private boolean receivesAcademicReports = true;

    protected StudentGuardian() {
    }

    public StudentGuardian(
            Student student,
            Guardian guardian,
            RelationshipType relationshipType,
            boolean primaryContact,
            boolean hasCustody,
            boolean receivesBilling,
            boolean receivesAcademicReports) {
        this.student = student;
        this.guardian = guardian;
        this.relationshipType = relationshipType;
        this.primaryContact = primaryContact;
        this.hasCustody = hasCustody;
        this.receivesBilling = receivesBilling;
        this.receivesAcademicReports = receivesAcademicReports;
    }

    public Student getStudent() {
        return student;
    }

    public Guardian getGuardian() {
        return guardian;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }

    public boolean isPrimaryContact() {
        return primaryContact;
    }

    public boolean isHasCustody() {
        return hasCustody;
    }

    public boolean isReceivesBilling() {
        return receivesBilling;
    }

    public boolean isReceivesAcademicReports() {
        return receivesAcademicReports;
    }
}
