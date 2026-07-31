package com.drakalabs.schoolmngsys.auth.service;

import com.drakalabs.schoolmngsys.auth.repository.RoleRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleQueryService {

    private final RoleRepository roleRepository;

    public RoleQueryService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public List<RoleView> list() {
        return roleRepository.findAll().stream().map(RoleView::from).toList();
    }
}
