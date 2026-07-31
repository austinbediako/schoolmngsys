package com.drakalabs.schoolmngsys.shared.security;

import java.util.Optional;

/** The permission-gate + scope-filter entry point every module's services use to ask "who is calling me". */
public interface CurrentAccountProvider {

    Optional<AuthenticatedAccount> current();
}
