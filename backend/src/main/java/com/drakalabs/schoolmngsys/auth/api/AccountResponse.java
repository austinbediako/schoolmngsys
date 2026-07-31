package com.drakalabs.schoolmngsys.auth.api;

import com.drakalabs.schoolmngsys.auth.domain.AccountStatus;
import com.drakalabs.schoolmngsys.auth.service.AccountView;
import com.drakalabs.schoolmngsys.shared.security.PersonType;
import java.util.UUID;

public record AccountResponse(
        UUID id, PersonType personType, UUID personId, String loginIdentifier, AccountStatus status) {

    public static AccountResponse from(AccountView view) {
        return new AccountResponse(view.id(), view.personType(), view.personId(), view.loginIdentifier(), view.status());
    }
}
