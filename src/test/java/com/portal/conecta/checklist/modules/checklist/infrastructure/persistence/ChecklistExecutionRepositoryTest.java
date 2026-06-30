package com.portal.conecta.checklist.modules.checklist.infrastructure.persistence;

import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.shared.context.AbstractRepositoryTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChecklistExecutionRepositoryTest extends AbstractRepositoryTest {

    private static final Pattern NAMED_PARAMETER = Pattern.compile("(?<!:):([A-Za-z][A-Za-z0-9_]*)");

    @Test
    @DisplayName("deve ser um repository Spring Data JPA")
    void deveSerRepositorySpringDataJpa() {
        assertTrue(JpaRepository.class.isAssignableFrom(ChecklistExecutionRepository.class));
    }

    @Test
    @DisplayName("deve declarar query nativa para evitar checklist duplicado")
    void deveDeclararQueryNativaParaEvitarChecklistDuplicado() throws NoSuchMethodException {
        Method method = ChecklistExecutionRepository.class.getMethod(
                "existsDuplicateChecklist",
                UUID.class,
                UUID.class,
                String.class,
                String.class,
                LocalDateTime.class,
                LocalDateTime.class
        );

        String query = normalizedQuery(method);

        assertAll(
                () -> assertEquals(boolean.class, method.getReturnType()),
                () -> assertNativeQuery(method),
                () -> assertQueryParamsMatchMethodParams(method),
                () -> assertTrue(query.contains("from checklist_execution ce")),
                () -> assertTrue(query.contains("ce.class_id = :classid")),
                () -> assertTrue(query.contains("ce.room_id = :roomid")),
                () -> assertTrue(query.contains("ce.period = :period")),
                () -> assertTrue(query.contains("ce.checklist_type = :checklisttype")),
                () -> assertTrue(query.contains("ce.started_at >= :startofday")),
                () -> assertTrue(query.contains("ce.started_at < :endofday")),
                () -> assertTrue(query.contains("ce.status <> 'canceled'"))
        );
    }

    @Test
    @DisplayName("deve declarar indice unico parcial para bloquear duplicidade concorrente")
    void deveDeclararIndiceUnicoParcialParaBloquearDuplicidadeConcorrente() throws Exception {
        String schema = Files.readString(Path.of("src/main/resources/schema-postgresql.sql"))
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase(Locale.ROOT);

        assertAll(
                () -> assertTrue(schema.contains("create unique index if not exists uidx_execution_no_duplicate")),
                () -> assertTrue(schema.contains("on checklist_execution")),
                () -> assertTrue(schema.contains("class_id")),
                () -> assertTrue(schema.contains("room_id")),
                () -> assertTrue(schema.contains("period")),
                () -> assertTrue(schema.contains("checklist_type")),
                () -> assertTrue(schema.contains("started_at::date")),
                () -> assertTrue(schema.contains("where status <> 'canceled'"))
        );
    }

    @Test
    @DisplayName("deve declarar query nativa para tres dias sem envio")
    void deveDeclararQueryNativaParaTresDiasSemEnvio() throws NoSuchMethodException {
        Method method = ChecklistExecutionRepository.class.getMethod(
                "hasThreeConsecutiveDaysWithoutSubmission",
                UUID.class
        );

        String query = normalizedQuery(method);

        assertAll(
                () -> assertEquals(boolean.class, method.getReturnType()),
                () -> assertNativeQuery(method),
                () -> assertQueryParamsMatchMethodParams(method),
                () -> assertTrue(query.contains("generate_series(1, 3)")),
                () -> assertTrue(query.contains("where not exists")),
                () -> assertTrue(query.contains("ce.user_id = :userid")),
                () -> assertTrue(query.contains("ce.status = 'submitted'")),
                () -> assertTrue(query.contains("ce.submitted_at >= d.day")),
                () -> assertTrue(query.contains("ce.submitted_at < d.day + interval '1 day'"))
        );
    }

    @Test
    @DisplayName("deve declarar query nativa para buscar por turma e periodo do dia")
    void deveDeclararQueryNativaParaBuscarPorTurmaEPeriodoDoDia() throws NoSuchMethodException {
        Method method = ChecklistExecutionRepository.class.getMethod(
                "findByClassAndDateNative",
                UUID.class,
                LocalDateTime.class,
                LocalDateTime.class
        );

        String query = normalizedQuery(method);

        assertAll(
                () -> assertEquals(List.class, method.getReturnType()),
                () -> assertTrue(method.getGenericReturnType().getTypeName().contains(ChecklistExecution.class.getName())),
                () -> assertNativeQuery(method),
                () -> assertQueryParamsMatchMethodParams(method),
                () -> assertTrue(query.contains("from checklist_execution ce")),
                () -> assertTrue(query.contains("ce.class_id = :classid")),
                () -> assertTrue(query.contains("ce.started_at >= :startofday")),
                () -> assertTrue(query.contains("ce.started_at < :endofday")),
                () -> assertTrue(query.contains("order by ce.started_at asc"))
        );
    }

    @Test
    @DisplayName("deve declarar query nativa para nao conforme sem justificativa")
    void deveDeclararQueryNativaParaNaoConformeSemJustificativa() throws NoSuchMethodException {
        Method method = ChecklistExecutionRepository.class.getMethod(
                "hasNonConformingItemWithoutJustification",
                UUID.class
        );

        String query = normalizedQuery(method);

        assertAll(
                () -> assertEquals(boolean.class, method.getReturnType()),
                () -> assertNativeQuery(method),
                () -> assertQueryParamsMatchMethodParams(method),
                () -> assertTrue(query.contains("jsonb_array_elements(ce.answers_json -> 'items')")),
                () -> assertTrue(query.contains("ce.id = :executionid")),
                () -> assertTrue(query.contains("(item.value ->> 'conforme')::boolean = false")),
                () -> assertTrue(query.contains("item.value ->> 'justificativa'"))
        );
    }

    private static void assertNativeQuery(Method method) {
        Query query = method.getAnnotation(Query.class);

        assertNotNull(query);
        assertTrue(query.nativeQuery());
    }

    private static void assertQueryParamsMatchMethodParams(Method method) {
        Query query = method.getAnnotation(Query.class);

        Set<String> queryParams = extractQueryParams(query.value());
        Set<String> methodParams = Arrays.stream(method.getParameters())
                .flatMap(parameter -> Arrays.stream(parameter.getAnnotationsByType(Param.class)))
                .map(Param::value)
                .collect(Collectors.toCollection(TreeSet::new));

        assertEquals(queryParams, methodParams);
    }

    private static Set<String> extractQueryParams(String query) {
        Matcher matcher = NAMED_PARAMETER.matcher(query);
        Set<String> params = new TreeSet<>();

        while (matcher.find()) {
            params.add(matcher.group(1));
        }

        return params;
    }

    private static String normalizedQuery(Method method) {
        return method.getAnnotation(Query.class)
                .value()
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}
