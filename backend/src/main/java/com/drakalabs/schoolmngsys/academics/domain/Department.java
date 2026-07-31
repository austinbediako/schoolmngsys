package com.drakalabs.schoolmngsys.academics.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** One of the four organizational units: NURSERY, KG, PRIMARY, JHS (seeded, docs/glossary.md). */
@Entity
@Table(name = "departments")
public class Department extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    protected Department() {
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
