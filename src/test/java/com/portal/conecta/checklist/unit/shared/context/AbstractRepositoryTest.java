package com.portal.conecta.checklist.unit.shared.context;

import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=none"
})
public abstract class AbstractRepositoryTest {


    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = createPostgresContainer();

    @SuppressWarnings("resource")
    private static PostgreSQLContainer<?> createPostgresContainer() {
        return new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("checklist_test")
            .withUsername("test")
            .withPassword("test")
            .withSharedMemorySize(268435456L)
            .withLogConsumer(frame -> System.out.print("[postgres] " + frame.getUtf8String()));
    }
}


