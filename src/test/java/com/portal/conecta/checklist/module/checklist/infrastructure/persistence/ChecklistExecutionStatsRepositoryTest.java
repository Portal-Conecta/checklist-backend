package com.portal.conecta.checklist.module.checklist.infrastructure.persistence;

import com.portal.conecta.checklist.module.checklist.application.port.out.persistence.ChecklistExecutionStatsPort;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionStatsRepository;
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
    @DisplayName("deve declarar m�todo countByDay com par�metros LocalDate")
    void deveDeclararCountByDay() throws NoSuchMethodException {
        Method method = ChecklistExecutionStatsPort.class.getMethod("countByDay", LocalDate.class, LocalDate.class);
        assertAll(
                () -> assertNotNull(method),
                () -> assertTrue(List.class.isAssignableFrom(method.getReturnType()))
        );
    }

    @Test
    @DisplayName("deve declarar m�todo countByStatus sem par�metros")
    void deveDeclararCountByStatus() throws NoSuchMethodException {
        Method method = ChecklistExecutionStatsPort.class.getMethod("countByStatus");
        assertAll(
                () -> assertNotNull(method),
                () -> assertTrue(List.class.isAssignableFrom(method.getReturnType()))
        );
    }

    @Test
    @DisplayName("deve declarar m�todo completionRate sem par�metros")
    void deveDeclararCompletionRate() throws NoSuchMethodException {
        Method method = ChecklistExecutionStatsPort.class.getMethod("completionRate");
        assertNotNull(method);
    }

    @Test
    @DisplayName("deve declarar m�todo avgFillTimeByDay com par�metros LocalDate")
    void deveDeclararAvgFillTimeByDay() throws NoSuchMethodException {
        Method method = ChecklistExecutionStatsPort.class.getMethod("avgFillTimeByDay", LocalDate.class, LocalDate.class);
        assertAll(
                () -> assertNotNull(method),
                () -> assertTrue(List.class.isAssignableFrom(method.getReturnType()))
        );
    }

    @Test
    @DisplayName("deve declarar m�todo withIssuesRate sem par�metros")
    void deveDeclararWithIssuesRate() throws NoSuchMethodException {
        Method method = ChecklistExecutionStatsPort.class.getMethod("withIssuesRate");
        assertNotNull(method);
    }

    @Test
    @DisplayName("deve declarar metodo heatmapShiftByDayOfWeek sem parametros")
    void deveDeclararHeatmapShiftByDayOfWeek() throws NoSuchMethodException {
        Method method = ChecklistExecutionStatsPort.class.getMethod("heatmapShiftByDayOfWeek");
        assertAll(
                () -> assertNotNull(method),
                () -> assertTrue(List.class.isAssignableFrom(method.getReturnType()))
        );
    }

    @Test
    @DisplayName("deve declarar metodo complianceByShift sem parametros")
    void deveDeclararComplianceByShift() throws NoSuchMethodException {
        Method method = ChecklistExecutionStatsPort.class.getMethod("complianceByShift");
        assertAll(
                () -> assertNotNull(method),
                () -> assertTrue(List.class.isAssignableFrom(method.getReturnType()))
        );
    }

    @Test
    @DisplayName("deve declarar metodo complianceTrendByWeek com parametros LocalDate")
    void deveDeclararComplianceTrendByWeek() throws NoSuchMethodException {
        Method method = ChecklistExecutionStatsPort.class.getMethod("complianceTrendByWeek", LocalDate.class, LocalDate.class);
        assertAll(
                () -> assertNotNull(method),
                () -> assertTrue(List.class.isAssignableFrom(method.getReturnType()))
        );
    }
}
