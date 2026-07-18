package com.portal.conecta.checklist.shared.config;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Carrega variaveis de um arquivo {@code .env} local para dentro do
 * {@link ConfigurableEnvironment} do Spring, permitindo rodar o projeto em
 * ambientes locais sem depender de configuracao global da maquina.
 *
 * <p>As entradas sao registradas como {@link SystemEnvironmentPropertySource}
 * -- nao como System properties via {@code System.setProperty} -- porque essa
 * e a unica classe de PropertySource para a qual o Spring Boot aplica a
 * conversao {@code SCREAMING_SNAKE_CASE -> dotted.case} usada pelo relaxed
 * binding de {@code @ConfigurationProperties} (ex.: {@code APP_RABBITMQ_QUEUE}
 * -> {@code app.rabbitmq.queue}). Um {@code Map}/System property generico so
 * resolve placeholders diretos ({@code ${DB_HOST}}), nao relaxed binding.</p>
 *
 * <p>Roda como {@link EnvironmentPostProcessor} (registrado em
 * {@code META-INF/spring.factories}, chave {@code org.springframework.boot.EnvironmentPostProcessor}
 * -- pacote novo desde a 4.0.0, a forma antiga em {@code org.springframework.boot.env}
 * esta deprecated/forRemoval) em vez de ser chamado a partir do {@code main()}, para executar antes do
 * processamento dos arquivos {@code application*.yml} -- garantindo que
 * {@code SPRING_PROFILES_ACTIVE} definido no {@code .env} ja influencie qual
 * perfil e ativado.</p>
 */
public class DotEnvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String ENV_FILE_NAME = ".env";
    private static final String PROPERTY_SOURCE_NAME = "dotenvFile";

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        load(environment, Path.of("").toAbsolutePath().resolve(ENV_FILE_NAME));
    }

    public void load(ConfigurableEnvironment environment, Path envFile) {
        if (envFile == null || !Files.isRegularFile(envFile)) {
            System.out.println("[DotEnv] Arquivo .env nao encontrado em: " + (envFile != null ? envFile.toAbsolutePath() : "null"));
            return;
        }

        Map<String, Object> values;
        try {
            System.out.println("[DotEnv] Carregando variaveis de: " + envFile.toAbsolutePath());
            values = parse(Files.readAllLines(envFile));
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel carregar o arquivo .env.", exception);
        }

        if (values.isEmpty()) {
            return;
        }

        register(environment, values);
    }

    private void register(ConfigurableEnvironment environment, Map<String, Object> values) {
        MutablePropertySources sources = environment.getPropertySources();
        sources.remove(PROPERTY_SOURCE_NAME);

        SystemEnvironmentPropertySource dotenvSource =
            new SystemEnvironmentPropertySource(PROPERTY_SOURCE_NAME, values);

        if (sources.contains(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)) {
            sources.addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, dotenvSource);
        } else {
            sources.addLast(dotenvSource);
        }
    }

    static Map<String, Object> parse(List<String> lines) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (String rawLine : lines) {
            parseLine(rawLine, values);
        }
        return values;
    }

    private static void parseLine(String rawLine, Map<String, Object> values) {
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

        if (!isValidKey(key)) {
            return;
        }

        values.put(key, unquote(value));
    }

    private static boolean isValidKey(String key) {
        return key.matches("[A-Za-z_][A-Za-z0-9_]*");
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
