package com.drakalabs.schoolmngsys.academics.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A concrete teaching group at one {@link ClassLevel} (BR-AS-004) — e.g. "Primary 3A". Durable
 * across years; class-teacher assignment and enrollments are what's year-scoped (docs/02 §5).
 * Named {@code SchoolClass} (docs call it "Class") to avoid shadowing {@code java.lang.Class}.
 */
@Entity
@Table(name = "classes")
public class SchoolClass extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_level_id", nullable = false)
    private ClassLevel classLevel;

    @Column(name = "stream", nullable = false)
    private String stream;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    protected SchoolClass() {
    }

    public SchoolClass(ClassLevel classLevel, String stream, int capacity) {
        this.classLevel = classLevel;
        this.stream = stream;
        this.capacity = capacity;
    }

    public ClassLevel getClassLevel() {
        return classLevel;
    }

    public String getStream() {
        return stream;
    }

    public int getCapacity() {
        return capacity;
    }
}
