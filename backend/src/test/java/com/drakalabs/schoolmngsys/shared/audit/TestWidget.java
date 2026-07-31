package com.drakalabs.schoolmngsys.shared.audit;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "test_widgets")
class TestWidget extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    protected TestWidget() {
    }

    TestWidget(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }
}
