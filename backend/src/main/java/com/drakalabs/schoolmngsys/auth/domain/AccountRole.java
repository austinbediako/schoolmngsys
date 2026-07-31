package com.drakalabs.schoolmngsys.auth.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * The mutable assignment of a {@link Role} to an {@link Account} (ROLE_ASSIGN). Revocation is
 * soft ({@code archive()}), preserving "who had what role when" — history is never destroyed.
 */
@Entity
@Table(name = "account_roles")
public class AccountRole extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    protected AccountRole() {
    }

    public AccountRole(Account account, Role role) {
        this.account = account;
        this.role = role;
    }

    public Account getAccount() {
        return account;
    }

    public Role getRole() {
        return role;
    }
}
