package com.drakalabs.schoolmngsys;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base for tests needing a real PostgreSQL (CLAUDE.md testing philosophy — never H2, dialect
 * fidelity matters for Flyway + jsonb + generated UUIDs). One container shared across the whole
 * JVM run via {@link PostgresTestContainer}'s singleton pattern — see its Javadoc for why a plain
 * {@code static @Container} field isn't actually JVM-wide despite looking like it.
 *
 * <p>The extra Flyway location picks up test-only fixture tables (e.g. {@code test_widgets})
 * alongside the real migrations — deliberately via {@code @TestPropertySource}, not a test-scope
 * {@code application.yml}, because a same-named file on the test classpath replaces (not layers
 * on top of) {@code src/main/resources/application.yml} and silently drops every other property.
 */
@SpringBootTest
@TestPropertySource(properties = "spring.flyway.locations=classpath:db/migration,classpath:db/testmigration")
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = PostgresTestContainer.instance();
}
