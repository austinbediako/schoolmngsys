package com.drakalabs.schoolmngsys.shared.web.error;

import com.drakalabs.schoolmngsys.shared.web.trace.TraceIdHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates every exception into the RFC 7807 contract (docs/10 §2): stable {@code type} slug
 * from {@link ProblemType}, {@code ruleId} for business-rule rejections, {@code traceId} for
 * correlation, {@code errors[]} for field violations. Never leaks SQL or stack traces.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldViolation> errors =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(fe -> new FieldViolation(fe.getField(), fe.getDefaultMessage()))
                        .toList();
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        ProblemType.VALIDATION.defaultStatus(), errors.size() + " field(s) invalid");
        applyCatalog(problem, ProblemType.VALIDATION, request);
        problem.setProperty("errors", errors);
        return respond(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        List<FieldViolation> errors =
                ex.getConstraintViolations().stream()
                        .map(v -> new FieldViolation(v.getPropertyPath().toString(), v.getMessage()))
                        .toList();
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        ProblemType.VALIDATION.defaultStatus(), errors.size() + " field(s) invalid");
        applyCatalog(problem, ProblemType.VALIDATION, request);
        problem.setProperty("errors", errors);
        return respond(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleMalformedBody(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(ProblemType.VALIDATION.defaultStatus(), "Malformed request body");
        applyCatalog(problem, ProblemType.VALIDATION, request);
        return respond(problem);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(NotFoundException ex, HttpServletRequest request) {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(ProblemType.NOT_FOUND.defaultStatus(), ex.getMessage());
        applyCatalog(problem, ProblemType.NOT_FOUND, request);
        return respond(problem);
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ProblemDetail> handleRuleViolation(
            BusinessRuleViolationException ex, HttpServletRequest request) {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(ProblemType.RULE_VIOLATION.defaultStatus(), ex.getMessage());
        applyCatalog(problem, ProblemType.RULE_VIOLATION, request);
        problem.setProperty("ruleId", ex.getRuleId());
        return respond(problem);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemDetail> handleConflict(ConflictException ex, HttpServletRequest request) {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(ProblemType.CONFLICT.defaultStatus(), ex.getMessage());
        applyCatalog(problem, ProblemType.CONFLICT, request);
        return respond(problem);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> handleOptimisticLock(
            OptimisticLockingFailureException ex, HttpServletRequest request) {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        ProblemType.CONFLICT.defaultStatus(), "The resource was modified by another request");
        applyCatalog(problem, ProblemType.CONFLICT, request);
        return respond(problem);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        ProblemType.CONFLICT.defaultStatus(), "The request conflicts with an existing record");
        applyCatalog(problem, ProblemType.CONFLICT, request);
        return respond(problem);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthRequired(
            AuthenticationException ex, HttpServletRequest request) {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        ProblemType.AUTH_REQUIRED.defaultStatus(), "Authentication is required");
        applyCatalog(problem, ProblemType.AUTH_REQUIRED, request);
        return respond(problem);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleForbidden(
            AccessDeniedException ex, HttpServletRequest request) {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        ProblemType.FORBIDDEN.defaultStatus(), "You do not have permission to perform this action");
        applyCatalog(problem, ProblemType.FORBIDDEN, request);
        return respond(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleInternal(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {}", request.getRequestURI(), ex);
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        ProblemType.INTERNAL.defaultStatus(), "An unexpected error occurred");
        applyCatalog(problem, ProblemType.INTERNAL, request);
        return respond(problem);
    }

    private void applyCatalog(ProblemDetail problem, ProblemType type, HttpServletRequest request) {
        problem.setType(type.uri());
        problem.setTitle(type.title());
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("traceId", TraceIdHolder.current());
    }

    private ResponseEntity<ProblemDetail> respond(ProblemDetail problem) {
        return ResponseEntity.status(problem.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}
