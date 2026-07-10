package com.portal.conecta.checklist.unit.checklist.infrastructure.persistence;

import com.portal.conecta.checklist.modules.checklist.issues.application.port.out.persistence.ChecklistIssueStatsPort;
import com.portal.conecta.checklist.modules.checklist.issues.infrastructure.persistence.ChecklistIssueStatsRepository;
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
    @DisplayName("deve declarar método countByDay com parâmetros LocalDate")
    void deveDeclararCountByDay() throws NoSuchMethodException {
        Method method = ChecklistIssueStatsPort.class.getMethod("countByDay", LocalDate.class, LocalDate.class);
        assertAll(
                () -> assertNotNull(method),
                () -> assertTrue(List.class.isAssignableFrom(method.getReturnType()))
        );
    }

    @Test
    @DisplayName("deve declarar método topFailingItems com parâmetro int")
    void deveDeclararTopFailingItems() throws NoSuchMethodException {
        Method method = ChecklistIssueStatsPort.class.getMethod("topFailingItems", int.class);
        assertAll(
                () -> assertNotNull(method),
                () -> assertTrue(List.class.isAssignableFrom(method.getReturnType()))
        );
    }

    @Test
    @DisplayName("deve declarar método resolutionSplit sem parâmetros")
    void deveDeclararResolutionSplit() throws NoSuchMethodException {
        Method method = ChecklistIssueStatsPort.class.getMethod("resolutionSplit");
        assertNotNull(method);
    }

    @Test
    @DisplayName("deve declarar método overdueCount sem parâmetros")
    void deveDeclararOverdueCount() throws NoSuchMethodException {
        Method method = ChecklistIssueStatsPort.class.getMethod("overdueCount");
        assertNotNull(method);
    }

    @Test
    @DisplayName("deve declarar método issuesPerExecution sem parâmetros")
    void deveDeclararIssuesPerExecution() throws NoSuchMethodException {
        Method method = ChecklistIssueStatsPort.class.getMethod("issuesPerExecution");
        assertNotNull(method);
    }

    @Test
    @DisplayName("deve declarar método countByChecklistType sem parâmetros")
    void deveDeclararCountByChecklistType() throws NoSuchMethodException {
        Method method = ChecklistIssueStatsPort.class.getMethod("countByChecklistType");
        assertAll(
                () -> assertNotNull(method),
                () -> assertTrue(List.class.isAssignableFrom(method.getReturnType()))
        );
    }
}
