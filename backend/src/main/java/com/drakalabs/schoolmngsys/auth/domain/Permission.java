package com.drakalabs.schoolmngsys.auth.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** A permission string from the catalog (docs/14 §6) — seeded, extended only by migration. */
@Entity
@Table(name = "permissions")
public class Permission extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    protected Permission() {
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
