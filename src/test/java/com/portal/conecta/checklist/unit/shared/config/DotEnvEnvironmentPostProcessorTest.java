package com.portal.conecta.checklist.unit.shared.config;

import com.portal.conecta.checklist.shared.config.DotEnvEnvironmentPostProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DotEnvEnvironmentPostProcessorTest {

    private final DotEnvEnvironmentPostProcessor postProcessor = new DotEnvEnvironmentPostProcessor();

    @TempDir
    Path tempDir;

    @Test
    void shouldExposeRawPlaceholdersFromEnvFile() throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, """
                # ignored
                DB_HOST=localhost
                export DB_NAME="checklist_db"
                INVALID LINE
                """);

        MockEnvironment environment = new MockEnvironment();
        postProcessor.load(environment, envFile);

        assertThat(environment.getProperty("DB_HOST")).isEqualTo("localhost");
        assertThat(environment.getProperty("DB_NAME")).isEqualTo("checklist_db");
    }

    @Test
    void shouldResolveConfigurationPropertiesViaRelaxedBinding() throws IOException {
        // Este e o caso que o antigo EnvFileLoader (System.setProperty) nao resolvia:
        // @ConfigurationProperties(prefix = "app.rabbitmq") so encontra APP_RABBITMQ_QUEUE
        // quando ele vem de uma fonte reconhecida como variavel de ambiente.
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "APP_RABBITMQ_QUEUE=notifications.dispatch.q");

        MockEnvironment environment = new MockEnvironment();
        postProcessor.load(environment, envFile);

        String bound = Binder.get(environment)
            .bind("app.rabbitmq.queue", String.class)
            .orElse(null);

        assertThat(bound).isEqualTo("notifications.dispatch.q");
    }

    @Test
    void shouldNotOverridePreviouslyDefinedProperties() throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "DB_HOST=from-.env-file");

        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("DB_HOST", "already-defined");
        postProcessor.load(environment, envFile);

        assertThat(environment.getProperty("DB_HOST")).isEqualTo("already-defined");
    }

    @Test
    void shouldMapSpringProfilesActiveToActiveProfile() throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "SPRING_PROFILES_ACTIVE=dev");

        MockEnvironment environment = new MockEnvironment();
        postProcessor.load(environment, envFile);

        assertThat(Binder.get(environment).bind("spring.profiles.active", String.class).orElse(null))
            .isEqualTo("dev");
    }

    @Test
    void shouldDoNothingWhenEnvFileIsMissing() {
        MockEnvironment environment = new MockEnvironment();
        postProcessor.load(environment, tempDir.resolve(".env"));

        assertThat(environment.getProperty("DB_HOST")).isNull();
    }
}
