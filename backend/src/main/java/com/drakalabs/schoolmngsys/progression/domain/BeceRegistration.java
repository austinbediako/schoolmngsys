package com.drakalabs.schoolmngsys.progression.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "bece_registrations")
public class BeceRegistration extends BaseEntity {

    @Column(name = "enrollment_id", nullable = false)
    private UUID enrollmentId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "index_number", nullable = false)
    private String indexNumber;

    @Column(name = "snapshot_first_name", nullable = false)
    private String snapshotFirstName;

    @Column(name = "snapshot_last_name", nullable = false)
    private String snapshotLastName;

    @Column(name = "snapshot_dob", nullable = false)
    private LocalDate snapshotDob;

    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt;

    protected BeceRegistration() {
    }

    public BeceRegistration(
            UUID enrollmentId,
            UUID studentId,
            String indexNumber,
            String snapshotFirstName,
            String snapshotLastName,
            LocalDate snapshotDob) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.indexNumber = indexNumber;
        this.snapshotFirstName = snapshotFirstName;
        this.snapshotLastName = snapshotLastName;
        this.snapshotDob = snapshotDob;
        this.registeredAt = Instant.now();
    }

    public UUID getEnrollmentId() {
        return enrollmentId;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public String getIndexNumber() {
        return indexNumber;
    }

    public String getSnapshotFirstName() {
        return snapshotFirstName;
    }

    public String getSnapshotLastName() {
        return snapshotLastName;
    }

    public LocalDate getSnapshotDob() {
        return snapshotDob;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public void updateIndexNumber(String indexNumber) {
        this.indexNumber = indexNumber;
    }
}
