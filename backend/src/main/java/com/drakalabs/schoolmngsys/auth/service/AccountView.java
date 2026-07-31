package com.drakalabs.schoolmngsys.auth.service;

import com.drakalabs.schoolmngsys.auth.domain.Account;
import com.drakalabs.schoolmngsys.auth.domain.AccountStatus;
import com.drakalabs.schoolmngsys.shared.security.PersonType;
import java.util.UUID;

/** Entity-free projection of an account — what {@code api} is allowed to see (docs/08 §2). */
public record AccountView(UUID id, PersonType personType, UUID personId, String loginIdentifier, AccountStatus status) {

    public static AccountView from(Account account) {
        return new AccountView(
                account.getId(),
                account.getPersonType(),
                account.getPersonId(),
                account.getLoginIdentifier(),
                account.getStatus());
    }
}
