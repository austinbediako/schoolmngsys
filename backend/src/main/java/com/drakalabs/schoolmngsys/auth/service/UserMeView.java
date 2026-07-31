package com.drakalabs.schoolmngsys.auth.service;

import java.util.Set;
import java.util.UUID;

public record UserMeView(
        UUID id,
        String username,
        String firstName,
        String lastName,
        String email,
        String phone,
        String personType,
        Set<String> roles,
        Set<String> permissions
) {}
