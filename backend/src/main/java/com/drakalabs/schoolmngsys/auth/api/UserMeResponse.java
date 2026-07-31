package com.drakalabs.schoolmngsys.auth.api;

import com.drakalabs.schoolmngsys.auth.service.UserMeView;
import java.util.Set;
import java.util.UUID;

public record UserMeResponse(UserDto user) {

    public record UserDto(
            UUID id,
            String username,
            String firstName,
            String lastName,
            String email,
            String phone,
            String personType,
            Set<String> roles,
            Set<String> permissions
    ) {
        public static UserDto from(UserMeView view) {
            return new UserDto(
                    view.id(),
                    view.username(),
                    view.firstName(),
                    view.lastName(),
                    view.email(),
                    view.phone(),
                    view.personType(),
                    view.roles(),
                    view.permissions()
            );
        }
    }

    public static UserMeResponse from(UserMeView view) {
        return new UserMeResponse(UserDto.from(view));
    }
}
