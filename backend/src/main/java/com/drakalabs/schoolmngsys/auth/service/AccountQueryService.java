package com.drakalabs.schoolmngsys.auth.service;

import com.drakalabs.schoolmngsys.auth.repository.AccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountQueryService {

    private final AccountRepository accountRepository;

    public AccountQueryService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public Page<AccountView> list(Pageable pageable) {
        return accountRepository.findAll(pageable).map(AccountView::from);
    }
}
