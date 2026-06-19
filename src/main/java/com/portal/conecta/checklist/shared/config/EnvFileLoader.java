package com.portal.conecta.checklist.shared.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Utilitario para carregar variaveis locais a partir de um arquivo {@code .env}.
 *
 * <p>Define propriedades de sistema antes da inicializacao do Spring, sem
 * sobrescrever variaveis ja configuradas no ambiente da JVM ou do sistema.</p>
 */
public final class EnvFileLoader {

    private static final String ENV_FILE_NAME = ".env";

    private EnvFileLoader() {
    }

    public static void loadFromWorkingDirectory() {
        load(Path.of("").toAbsolutePath().resolve(ENV_FILE_NAME));
    }

    public static void load(Path envFile) {
        if (envFile == null || !Files.isRegularFile(envFile)) {
            return;
        }

        try {
            loadLines(Files.readAllLines(envFile));
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel carregar o arquivo .env.", exception);
        }
    }

    private static void loadLines(List<String> lines) {
        for (String line : lines) {
            loadLine(line);
        }
    }

    private static void loadLine(String rawLine) {
        if (rawLine == null) {
            return;
        }

        String line = rawLine.trim();

        if (line.isBlank() || line.startsWith("#")) {
            return;
        }

        if (line.startsWith("export ")) {
            line = line.substring("export ".length()).trim();
        }

        int separatorIndex = line.indexOf('=');

        if (separatorIndex <= 0) {
            return;
        }

        String key = line.substring(0, separatorIndex).trim();
        String value = line.substring(separatorIndex + 1).trim();

        if (!isValidKey(key) || alreadyDefined(key)) {
            return;
        }

        String resolvedValue = unquote(value);

        System.setProperty(key, resolvedValue);
        setSpringAliasIfNeeded(key, resolvedValue);
    }

    private static void setSpringAliasIfNeeded(String key, String value) {
        if ("SPRING_PROFILES_ACTIVE".equals(key) && System.getProperty("spring.profiles.active") == null) {
            System.setProperty("spring.profiles.active", value);
        }
    }

    private static boolean isValidKey(String key) {
        return key.matches("[A-Za-z_][A-Za-z0-9_]*");
    }

    private static boolean alreadyDefined(String key) {
        return System.getProperty(key) != null || System.getenv(key) != null;
    }

    private static String unquote(String value) {
        if (value.length() < 2) {
            return value;
        }

        boolean doubleQuoted = value.startsWith("\"") && value.endsWith("\"");
        boolean singleQuoted = value.startsWith("'") && value.endsWith("'");

        if (doubleQuoted || singleQuoted) {
            return value.substring(1, value.length() - 1);
        }

        return value;
    }
}
