package com.portal.conecta.checklist.shared.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class EnvFileLoaderTest {

    private static final String SAMPLE_KEY = "CHECKLIST_ENV_FILE_LOADER_SAMPLE";
    private static final String EXISTING_KEY = "CHECKLIST_ENV_FILE_LOADER_EXISTING";
    private static final String QUOTED_KEY = "CHECKLIST_ENV_FILE_LOADER_QUOTED";

    @TempDir
    Path tempDir;

    @AfterEach
    void tearDown() {
        System.clearProperty(SAMPLE_KEY);
        System.clearProperty(EXISTING_KEY);
        System.clearProperty(QUOTED_KEY);
    }

    @Test
    void shouldLoadValidEnvFileEntries() throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, """
                # ignored
                CHECKLIST_ENV_FILE_LOADER_SAMPLE=loaded
                export CHECKLIST_ENV_FILE_LOADER_QUOTED="quoted value"
                INVALID LINE
                """);

        EnvFileLoader.load(envFile);

        assertThat(System.getProperty(SAMPLE_KEY)).isEqualTo("loaded");
        assertThat(System.getProperty(QUOTED_KEY)).isEqualTo("quoted value");
    }

    @Test
    void shouldNotOverridePreviouslyDefinedProperties() throws IOException {
        Path envFile = tempDir.resolve(".env");
        System.setProperty(EXISTING_KEY, "already-defined");
        Files.writeString(envFile, "CHECKLIST_ENV_FILE_LOADER_EXISTING=from-env-file");

        EnvFileLoader.load(envFile);

        assertThat(System.getProperty(EXISTING_KEY)).isEqualTo("already-defined");
    }
}
