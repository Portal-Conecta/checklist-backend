package com.portal.conecta.checklist.unit.checklist.infrastructure.persistence;

import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistExecutionStatsPort;
import com.portal.conecta.checklist.modules.checklist.infrastructure.persistence.ChecklistExecutionStatsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChecklistExecutionStatsRepositoryTest {

    @Test
    @DisplayName("deve implementar ChecklistExecutionStatsPort")
    void deveImplementarChecklistExecutionStatsPort() {
        assertTrue(ChecklistExecutionStatsPort.class.isAssignableFrom(ChecklistExecutionStatsRepository.class));
    }

    @Test
    @DisplayName("deve ser anotado com @Repository")
    void deveSerAnotadoComRepository() {
        assertNotNull(ChecklistExecutionStatsRepository.class.getAnnotation(Repository.class));
    }

    @Test
    @DisplayName("deve declarar método countByDay com parâmetros LocalDate")
    void deveDeclararCountByDay() throws NoSuchMethodException {
        Method method = ChecklistExecutionStatsPort.class.getMethod("countByDay", LocalDate.class, LocalDate.class);
        assertAll(
                () -> assertNotNull(method),
                () -> assertTrue(List.class.isAssignableFrom(method.getReturnType()))
        );
    }

    @Test
    @DisplayName("deve declarar método countByStatus sem parâmetros")
    void deveDeclararCountByStatus() throws NoSuchMethodException {
        Method method = ChecklistExecutionStatsPort.class.getMethod("countByStatus");
        assertAll(
                () -> assertNotNull(method),
                () -> assertTrue(List.class.isAssignableFrom(method.getReturnType()))
        );
    }

    @Test
    @DisplayName("deve declarar método completionRate sem parâmetros")
    void deveDeclararCompletionRate() throws NoSuchMethodException {
        Method method = ChecklistExecutionStatsPort.class.getMethod("completionRate");
        assertNotNull(method);
    }

    @Test
    @DisplayName("deve declarar método avgFillTimeByDay com parâmetros LocalDate")
    void deveDeclararAvgFillTimeByDay() throws NoSuchMethodException {
        Method method = ChecklistExecutionStatsPort.class.getMethod("avgFillTimeByDay", LocalDate.class, LocalDate.class);
        assertAll(
                () -> assertNotNull(method),
                () -> assertTrue(List.class.isAssignableFrom(method.getReturnType()))
        );
    }

    @Test
    @DisplayName("deve declarar método withIssuesRate sem parâmetros")
    void deveDeclararWithIssuesRate() throws NoSuchMethodException {
        Method method = ChecklistExecutionStatsPort.class.getMethod("withIssuesRate");
        assertNotNull(method);
    }

    @Test
    @DisplayName("deve declarar método heatmapShiftByDayOfWeek sem parâmetros")
    void deveDeclararHeatmapShiftByDayOfWeek() throws NoSuchMethodException {
        Method method = ChecklistExecutionStatsPort.class.getMethod("heatmapShiftByDayOfWeek");
        assertAll(
                () -> assertNotNull(method),
                () -> assertTrue(List.class.isAssignableFrom(method.getReturnType()))
        );
    }
}
