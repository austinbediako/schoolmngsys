package com.drakalabs.schoolmngsys.finance.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "fee_items")
public class FeeItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fee_schedule_id", nullable = false)
    private FeeSchedule feeSchedule;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "mandatory", nullable = false)
    private boolean mandatory = true;

    protected FeeItem() {
    }

    public FeeItem(FeeSchedule feeSchedule, String name, BigDecimal amount, boolean mandatory) {
        this.feeSchedule = feeSchedule;
        this.name = name;
        this.amount = amount;
        this.mandatory = mandatory;
    }

    public FeeSchedule getFeeSchedule() {
        return feeSchedule;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public boolean isMandatory() {
        return mandatory;
    }
}
