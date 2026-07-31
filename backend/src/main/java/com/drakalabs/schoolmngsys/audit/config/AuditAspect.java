package com.drakalabs.schoolmngsys.audit.config;

import com.drakalabs.schoolmngsys.audit.domain.AuditLog;
import com.drakalabs.schoolmngsys.audit.service.AuditRecorder;
import com.drakalabs.schoolmngsys.shared.audit.Audited;
import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import com.drakalabs.schoolmngsys.shared.security.CurrentActorProvider;
import java.util.Optional;
import java.util.UUID;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Intercepts every {@link Audited @Audited} service method across every module and records it
 * (BR-SE-002, ADR-007). Cross-cutting by construction: modules never call into {@code audit}
 * directly, they only carry the {@code @Audited} marker from {@code shared}.
 */
@Aspect
@Component
public class AuditAspect {

    private final AuditRecorder auditRecorder;
    private final CurrentActorProvider currentActorProvider;

    public AuditAspect(AuditRecorder auditRecorder, CurrentActorProvider currentActorProvider) {
        this.auditRecorder = auditRecorder;
        this.currentActorProvider = currentActorProvider;
    }

    @AfterReturning(pointcut = "@annotation(audited)", returning = "result")
    public void recordAudit(JoinPoint joinPoint, Audited audited, Object result) {
        AuditLog entry =
                new AuditLog(
                        currentActorProvider.currentActorId().orElse(null),
                        audited.action(),
                        audited.entityType(),
                        resolveEntityId(result, joinPoint.getArgs()),
                        null,
                        currentRequestIp().orElse(null));
        auditRecorder.record(entry);
    }

    private String resolveEntityId(Object result, Object[] args) {
        if (result instanceof BaseEntity entity) {
            return entity.getId().toString();
        }
        for (Object arg : args) {
            if (arg instanceof BaseEntity entity) {
                return entity.getId().toString();
            }
        }
        // Fall back to the first UUID argument (e.g. an id-first service method signature) so
        // methods returning a plain DTO/record still leave a traceable audit row.
        for (Object arg : args) {
            if (arg instanceof UUID uuid) {
                return uuid.toString();
            }
        }
        return null;
    }

    private Optional<String> currentRequestIp() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) {
            return Optional.empty();
        }
        return Optional.ofNullable(attrs.getRequest().getRemoteAddr());
    }
}
