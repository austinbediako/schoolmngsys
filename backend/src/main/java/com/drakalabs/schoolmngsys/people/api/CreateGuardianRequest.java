package com.drakalabs.schoolmngsys.people.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateGuardianRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Pattern(regexp = "^\\+233\\d{9}$", message = "must be a +233 E.164 phone number") String phone,
        @Email String email,
        String occupation,
        String address) {
}
