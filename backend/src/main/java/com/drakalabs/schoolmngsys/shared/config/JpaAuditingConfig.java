package com.drakalabs.schoolmngsys.shared.config;

import com.drakalabs.schoolmngsys.shared.security.CurrentActorProvider;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<UUID> auditorAware(CurrentActorProvider currentActorProvider) {
        return new AuditorAware<>() {
            @Override
            public Optional<UUID> getCurrentAuditor() {
                return currentActorProvider.currentActorId();
            }
        };
    }
}
