package com.drakalabs.schoolmngsys;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * True JVM-wide singleton Postgres container (Testcontainers' documented "Singleton Container"
 * pattern). A plain {@code static @Container} field looks JVM-wide but isn't: JUnit's
 * {@code TestcontainersExtension} stops it in {@code afterAll} of *each* test class, even when
 * the field is inherited from a shared base class — restarting the same instance for the next
 * class is unreliable and intermittently refuses connections. Overriding {@link #stop()} to
 * no-op makes every class's stop-attempt harmless; Ryuk still reaps the real container at JVM exit.
 */
final class PostgresTestContainer extends PostgreSQLContainer<PostgresTestContainer> {

    private static final PostgresTestContainer INSTANCE = new PostgresTestContainer();

    private PostgresTestContainer() {
        super("postgres:16-alpine");
    }

    static PostgresTestContainer instance() {
        if (!INSTANCE.isRunning()) {
            INSTANCE.start();
        }
        return INSTANCE;
    }

    @Override
    public void stop() {
        // Deliberately no-op — see class Javadoc.
    }
}
