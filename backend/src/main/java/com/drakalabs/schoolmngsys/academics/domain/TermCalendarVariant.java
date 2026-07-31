package com.drakalabs.schoolmngsys.academics.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

/** BR-AS-003: overrides a term's dates for one class level (JHS 3's earlier Terms 2-3 close). */
@Entity
@Table(name = "term_calendar_variants")
public class TermCalendarVariant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "term_id", nullable = false)
    private Term term;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_level_id", nullable = false)
    private ClassLevel classLevel;

    @Column(name = "override_start_date", nullable = false)
    private LocalDate overrideStartDate;

    @Column(name = "override_end_date", nullable = false)
    private LocalDate overrideEndDate;

    protected TermCalendarVariant() {
    }

    public TermCalendarVariant(Term term, ClassLevel classLevel, LocalDate overrideStartDate, LocalDate overrideEndDate) {
        this.term = term;
        this.classLevel = classLevel;
        this.overrideStartDate = overrideStartDate;
        this.overrideEndDate = overrideEndDate;
    }

    public Term getTerm() {
        return term;
    }

    public ClassLevel getClassLevel() {
        return classLevel;
    }

    public LocalDate getOverrideStartDate() {
        return overrideStartDate;
    }

    public LocalDate getOverrideEndDate() {
        return overrideEndDate;
    }
}
