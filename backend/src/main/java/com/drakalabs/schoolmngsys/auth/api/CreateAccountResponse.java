package com.drakalabs.schoolmngsys.auth.api;

import com.drakalabs.schoolmngsys.auth.service.AccountCreationResult;

public record CreateAccountResponse(AccountResponse account, String temporaryPassword) {

    public static CreateAccountResponse from(AccountCreationResult result) {
        return new CreateAccountResponse(AccountResponse.from(result.account()), result.temporaryPassword());
    }
}
