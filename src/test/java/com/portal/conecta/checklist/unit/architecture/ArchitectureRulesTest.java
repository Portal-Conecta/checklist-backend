package com.portal.conecta.checklist.unit.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ArchitectureRulesTest {

    private static final Path SOURCE_ROOT = Path.of("src", "main", "java");
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("(?m)^[\\t ]*package\\s+([^;]+);");
    private static final Pattern IMPORT_PATTERN = Pattern.compile("(?m)^[\\t ]*import\\s+(?:static\\s+)?([^;]+);");

    @Test
    void applicationMustNotDependOnPresentationOrInfrastructure() throws IOException {
        List<String> violations = javaSources()
                .filter(path -> normalized(path).contains("/application/"))
                .flatMap(path -> forbiddenImports(
                        path,
                        ".presentation.",
                        ".infrastructure."
                ))
                .toList();

        assertThat(violations).isEmpty();
    }

    @Test
    void domainMustNotDependOnOuterLayers() throws IOException {
        List<String> violations = javaSources()
                .filter(path -> normalized(path).contains("/domain/"))
                .flatMap(path -> forbiddenImports(
                        path,
                        ".application.",
                        ".presentation.",
                        ".infrastructure.",
                        ".shared.integration."
                ))
                .toList();

        assertThat(violations).isEmpty();
    }

    @Test
    void sourcePathMustMatchDeclaredPackage() throws IOException {
        List<String> violations = javaSources()
                .map(this::packagePathViolation)
                .filter(value -> !value.isBlank())
                .toList();

        assertThat(violations).isEmpty();
    }

    @Test
    void legacyPackagesMustNotReturn() throws IOException {
        List<String> forbiddenFragments = List.of(
                ".modules.checklist.",
                ".modules.issues.",
                ".dto.mudar",
                ".shared.client.",
                ".shared.hub.provider.",
                ".shared.hub.mock.",
                ".shared.integration.hub.mock."
        );

        List<String> violations = javaSources()
                .flatMap(path -> forbiddenFragments.stream()
                        .filter(fragment -> read(path).contains(fragment))
                        .map(fragment -> normalized(path) + " contains " + fragment))
                .toList();

        assertThat(violations).isEmpty();
    }

    /**
     * Reforca a fronteira entre os modulos Checklist e Issues (ver ADR-0020):
     * so podem se comunicar atraves das portas explicitas listadas abaixo, nunca
     * importando classes internas (dominio, casos de uso, repositorios) um do
     * outro.
     */
    @Test
    void issuesAndChecklistMustOnlyCommunicateThroughPorts() throws IOException {
        String checklistPrefix = "com.portal.conecta.checklist.module.checklist.";
        String issuesPrefix = "com.portal.conecta.checklist.module.issues.";

        List<String> checklistMayImportFromIssues = List.of(
                "com.portal.conecta.checklist.module.issues.application.port.out.execution.",
                "com.portal.conecta.checklist.module.issues.presentation.dto.response."
        );
        List<String> issuesMayImportFromChecklist = List.of(
                "com.portal.conecta.checklist.module.checklist.application.port.out.issue.",
                "com.portal.conecta.checklist.module.checklist.presentation.port."
        );

        List<String> violations = javaSources()
                .flatMap(path -> {
                    String normalizedPath = normalized(path);
                    boolean isChecklist = normalizedPath.contains("/module/checklist/");
                    boolean isIssues = normalizedPath.contains("/module/issues/");

                    if (isChecklist) {
                        return crossModuleImports(path, issuesPrefix, checklistMayImportFromIssues);
                    }
                    if (isIssues) {
                        return crossModuleImports(path, checklistPrefix, issuesMayImportFromChecklist);
                    }
                    return Stream.empty();
                })
                .toList();

        assertThat(violations).isEmpty();
    }

    private Stream<String> crossModuleImports(Path path, String otherModulePrefix, List<String> sanctioned) {
        return extractImports(path)
                .filter(imported -> imported.startsWith(otherModulePrefix))
                .filter(imported -> sanctioned.stream().noneMatch(imported::startsWith))
                .map(imported -> normalized(path) + " imports " + imported);
    }

    private Stream<String> extractImports(Path path) {
        List<String> imports = new ArrayList<>();
        Matcher matcher = IMPORT_PATTERN.matcher(read(path));
        while (matcher.find()) {
            imports.add(matcher.group(1));
        }
        return imports.stream();
    }

    private Stream<Path> javaSources() throws IOException {
        return Files.walk(SOURCE_ROOT)
                .filter(path -> path.toString().endsWith(".java"));
    }

    private Stream<String> forbiddenImports(Path path, String... fragments) {
        String source = read(path);
        return Stream.of(fragments)
                .filter(source::contains)
                .map(fragment -> normalized(path) + " imports " + fragment);
    }

    private String packagePathViolation(Path path) {
        String source = read(path);
        Matcher matcher = PACKAGE_PATTERN.matcher(source);

        if (!matcher.find()) {
            return normalized(path) + " has no package declaration";
        }

        Path expected = SOURCE_ROOT
                .resolve(matcher.group(1).replace('.', '/'))
                .resolve(path.getFileName().toString())
                .normalize();

        return expected.equals(path.normalize())
                ? ""
                : normalized(path) + " should be " + normalized(expected);
    }

    private String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read " + path, exception);
        }
    }

    private String normalized(Path path) {
        return path.toString().replace('\\', '/');
    }
}
