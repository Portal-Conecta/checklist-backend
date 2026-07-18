package com.portal.conecta.checklist.module.checklist.infrastructure.persistence;

import com.portal.conecta.checklist.module.issues.application.port.out.persistence.ChecklistIssueStatsPort;
import com.portal.conecta.checklist.module.issues.infrastructure.persistence.ChecklistIssueStatsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChecklistIssueStatsRepositoryTest {

    @Test
    @DisplayName("deve implementar ChecklistIssueStatsPort")
    void deveImplementarChecklistIssueStatsPort() {
        assertTrue(ChecklistIssueStatsPort.class.isAssignableFrom(ChecklistIssueStatsRepository.class));
    }

    @Test
    @DisplayName("deve ser anotado com @Repository")
    void deveSerAnotadoComRepository() {
        assertNotNull(ChecklistIssueStatsRepository.class.getAnnotation(Repository.class));
    }

    @Test
    @DisplayName("deve declarar m�todo countByDay com par�metros LocalDate")
    void deveDeclararCountByDay() throws NoSuchMethodException {
        Method method = ChecklistIssueStatsPort.class.getMethod("countByDay", LocalDate.class, LocalDate.class);
        assertAll(
                () -> assertNotNull(method),
                () -> assertTrue(List.class.isAssignableFrom(method.getReturnType()))
        );
    }

    @Test
    @DisplayName("deve declarar m�todo topFailingItems com par�metro int")
    void deveDeclararTopFailingItems() throws NoSuchMethodException {
        Method method = ChecklistIssueStatsPort.class.getMethod("topFailingItems", int.class);
        assertAll(
                () -> assertNotNull(method),
                () -> assertTrue(List.class.isAssignableFrom(method.getReturnType()))
        );
    }

    @Test
    @DisplayName("deve declarar m�todo resolutionSplit sem par�metros")
    void deveDeclararResolutionSplit() throws NoSuchMethodException {
        Method method = ChecklistIssueStatsPort.class.getMethod("resolutionSplit");
        assertNotNull(method);
    }

    @Test
    @DisplayName("deve declarar m�todo overdueCount sem par�metros")
    void deveDeclararOverdueCount() throws NoSuchMethodException {
        Method method = ChecklistIssueStatsPort.class.getMethod("overdueCount");
        assertNotNull(method);
    }

    @Test
    @DisplayName("deve declarar m�todo issuesPerExecution sem par�metros")
    void deveDeclararIssuesPerExecution() throws NoSuchMethodException {
        Method method = ChecklistIssueStatsPort.class.getMethod("issuesPerExecution");
        assertNotNull(method);
    }

    @Test
    @DisplayName("deve declarar m�todo countByChecklistType sem par�metros")
    void deveDeclararCountByChecklistType() throws NoSuchMethodException {
        Method method = ChecklistIssueStatsPort.class.getMethod("countByChecklistType");
        assertAll(
                () -> assertNotNull(method),
                () -> assertTrue(List.class.isAssignableFrom(method.getReturnType()))
        );
    }
}
