package com.drakalabs.schoolmngsys.shared.web.error;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Test-only fixture that exercises {@link GlobalExceptionHandler} against each problem type. */
@RestController
@RequestMapping("/__test/problems")
class ProblemContractTestController {

    @GetMapping("/not-found")
    void notFound() {
        throw new NotFoundException("no such widget");
    }

    @GetMapping("/rule-violation")
    void ruleViolation() {
        throw new BusinessRuleViolationException("BR-EN-001", "duplicate active enrollment");
    }

    @PostMapping("/validate")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    void validate(@RequestBody @Valid SampleRequest request) {
    }

    record SampleRequest(@NotBlank String name) {
    }
}
