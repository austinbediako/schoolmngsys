package com.drakalabs.schoolmngsys.academics.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** A NaCCA subject, applicable across a {@link ClassLevel#getSequence()} range. */
@Entity
@Table(name = "subjects")
public class Subject extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "min_level_sequence", nullable = false)
    private int minLevelSequence;

    @Column(name = "max_level_sequence", nullable = false)
    private int maxLevelSequence;

    protected Subject() {
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public boolean appliesTo(ClassLevel level) {
        int sequence = level.getSequence();
        return sequence >= minLevelSequence && sequence <= maxLevelSequence;
    }
}
