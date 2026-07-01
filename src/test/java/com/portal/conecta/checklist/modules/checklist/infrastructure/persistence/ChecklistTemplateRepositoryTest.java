package com.portal.conecta.checklist.modules.checklist.infrastructure.persistence;

import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
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

class ChecklistTemplateRepositoryTest {

    private static final Pattern NAMED_PARAMETER = Pattern.compile("(?<!:):([A-Za-z][A-Za-z0-9_]*)");

    @Test
    @DisplayName("deve ser um repository Spring Data JPA")
    void deveSerRepositorySpringDataJpa() {
        assertTrue(JpaRepository.class.isAssignableFrom(ChecklistTemplateRepository.class));
    }

    @Test
    @DisplayName("deve declarar todas as queries como nativas com params validos")
    void deveDeclararTodasAsQueriesComoNativasComParamsValidos() {
        Arrays.stream(ChecklistTemplateRepository.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Query.class))
                .forEach(method -> assertAll(
                        () -> assertNativeQuery(method),
                        () -> assertQueryParamsMatchMethodParams(method)
                ));
    }

    @Test
    @DisplayName("deve declarar query para contar templates ativos por sala")
    void deveDeclararQueryParaContarTemplatesAtivosPorSala() throws NoSuchMethodException {
        Method method = ChecklistTemplateRepository.class.getMethod(
                "countActiveTemplatesByRoomId",
                UUID.class
        );

        String query = normalizedQuery(method);

        assertAll(
                () -> assertEquals(Long.class, method.getReturnType()),
                () -> assertTrue(query.contains("from checklist_template")),
                () -> assertTrue(query.contains("room_id = :roomid")),
                () -> assertTrue(query.contains("active = true")),
                () -> assertTrue(query.contains("status = 'active'"))
        );
    }

    @Test
    @DisplayName("deve declarar query para buscar template ativo mais recente")
    void deveDeclararQueryParaBuscarTemplateAtivoMaisRecente() throws NoSuchMethodException {
        Method method = ChecklistTemplateRepository.class.getMethod(
                "findLatestActiveTemplateByRoomId",
                UUID.class
        );

        String query = normalizedQuery(method);

        assertAll(
                () -> assertEquals(Optional.class, method.getReturnType()),
                () -> assertTrue(method.getGenericReturnType().getTypeName().contains(UUID.class.getName())),
                () -> assertTrue(query.contains("select id")),
                () -> assertTrue(query.contains("from checklist_template")),
                () -> assertTrue(query.contains("room_id = :roomid")),
                () -> assertTrue(query.contains("active = true")),
                () -> assertTrue(query.contains("status = 'active'")),
                () -> assertTrue(query.contains("order by version desc")),
                () -> assertTrue(query.contains("limit 1"))
        );
    }

    @Test
    @DisplayName("deve declarar query para contar conflitos de templates ativos")
    void deveDeclararQueryParaContarConflitosDeTemplatesAtivos() throws NoSuchMethodException {
        Method method = ChecklistTemplateRepository.class.getMethod(
                "countActiveTemplateConflictsByRoomId",
                UUID.class
        );

        String query = normalizedQuery(method);

        assertAll(
                () -> assertEquals(Long.class, method.getReturnType()),
                () -> assertTrue(query.contains("select count(id)")),
                () -> assertTrue(query.contains("from checklist_template")),
                () -> assertTrue(query.contains("room_id = :roomid")),
                () -> assertTrue(query.contains("active = true")),
                () -> assertTrue(query.contains("status = 'active'")),
                () -> assertTrue(query.contains("group by room_id")),
                () -> assertTrue(query.contains("having count(id) > 1"))
        );
    }

    @Test
    @DisplayName("deve declarar query para buscar template por id")
    void deveDeclararQueryParaBuscarTemplatePorId() throws NoSuchMethodException {
        Method method = ChecklistTemplateRepository.class.getMethod(
                "findTemplateByIdNative",
                UUID.class
        );

        String query = normalizedQuery(method);

        assertAll(
                () -> assertEquals(Optional.class, method.getReturnType()),
                () -> assertTrue(method.getGenericReturnType().getTypeName().contains(ChecklistTemplate.class.getName())),
                () -> assertTrue(query.contains("from checklist_template")),
                () -> assertTrue(query.contains("where id = :templateid"))
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
                .map(param -> param.value())
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
