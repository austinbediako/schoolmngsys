package com.drakalabs.schoolmngsys.academics.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A rung on the fixed ladder N1 -> N2 -> KG1 -> KG2 -> B1..B9 (BR-AS-002). Reference data, seeded
 * by migration, never user-editable. {@code sequence} is the ordering promotion (WP-9) walks.
 */
@Entity
@Table(name = "class_levels")
public class ClassLevel extends BaseEntity {

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "canonical_name", nullable = false)
    private String canonicalName;

    @Column(name = "basic_alias")
    private String basicAlias;

    @Column(name = "sequence", nullable = false)
    private int sequence;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    protected ClassLevel() {
    }

    public String getCode() {
        return code;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    public String getBasicAlias() {
        return basicAlias;
    }

    public int getSequence() {
        return sequence;
    }

    public Department getDepartment() {
        return department;
    }
}
