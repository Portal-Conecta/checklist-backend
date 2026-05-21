package com.portal.conecta.checklist.module.issues.presentation.dto.response;

import com.portal.conecta.checklist.module.issues.presentation.dto.enums.IssuePriority;
import com.portal.conecta.checklist.module.issues.presentation.dto.enums.IssueStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class IssueResponseDTOTest {

    private final LocalDateTime now = LocalDateTime.now();

    @Test
    @DisplayName("should correctly hold all fields when fully populated")
    void shouldCorrectlyHoldAllFieldsWhenFullyPopulated() {
        UUID id = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        UUID assignedTo = UUID.randomUUID();
        LocalDateTime dueAt = now.plusDays(30);

        IssueResponseDTO dto = new IssueResponseDTO(
                id,
                executionId,
                "item_007",
                "Uso de EPI obrigatório",
                assignedTo,
                "EPI ausente no posto de trabalho",
                "Colaborador sem capacete durante atividade de risco.",
                IssueStatus.OPEN,
                IssuePriority.HIGH,
                dueAt,
                null,
                now,
                now
        );

        assertEquals(id, dto.id());
        assertEquals(executionId, dto.executionId());
        assertEquals("item_007", dto.itemKey());
        assertEquals("Uso de EPI obrigatório", dto.itemTitleSnapshot());
        assertEquals(assignedTo, dto.assignedTo());
        assertEquals("EPI ausente no posto de trabalho", dto.title());
        assertEquals("Colaborador sem capacete durante atividade de risco.", dto.description());
        assertEquals(IssueStatus.OPEN, dto.status());
        assertEquals(IssuePriority.HIGH, dto.priority());
        assertEquals(dueAt, dto.dueAt());
        assertNull(dto.resolvedAt());
        assertEquals(now, dto.createdAt());
        assertEquals(now, dto.updatedAt());
    }

    @Test
    @DisplayName("should populate resolvedAt when issue transitions to RESOLVED")
    void shouldPopulateResolvedAtWhenIssueTransitionsToResolved() {
        LocalDateTime resolvedAt = now.plusDays(5);

        IssueResponseDTO dto = new IssueResponseDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "item_007",
                "Uso de EPI obrigatório",
                UUID.randomUUID(),
                "EPI ausente no posto de trabalho",
                "Colaborador sem capacete.",
                IssueStatus.RESOLVED,
                IssuePriority.MEDIUM,
                now.plusDays(30),
                resolvedAt,
                now,
                resolvedAt
        );

        assertEquals(IssueStatus.RESOLVED, dto.status());
        assertNotNull(dto.resolvedAt());
        assertEquals(resolvedAt, dto.resolvedAt());
    }

    @Test
    @DisplayName("should keep resolvedAt null for non-resolved statuses")
    void shouldKeepResolvedAtNullForNonResolvedStatuses() {
        for (IssueStatus status : new IssueStatus[]{IssueStatus.OPEN, IssueStatus.IN_PROGRESS, IssueStatus.REOPENED, IssueStatus.CANCELED}) {
            IssueResponseDTO dto = new IssueResponseDTO(
                    UUID.randomUUID(), UUID.randomUUID(), "item_001", "Título snapshot",
                    UUID.randomUUID(), "Título da pendência", "Descrição.",
                    status, IssuePriority.LOW, now.plusDays(10), null, now, now
            );

            assertNull(dto.resolvedAt(), "resolvedAt deveria ser null para o status: " + status);
        }
    }

    @Test
    @DisplayName("should reflect all IssueStatus values correctly")
    void shouldReflectAllIssueStatusValuesCorrectly() {
        for (IssueStatus status : IssueStatus.values()) {
            IssueResponseDTO dto = new IssueResponseDTO(
                    UUID.randomUUID(), UUID.randomUUID(), "item_001", "Título snapshot",
                    UUID.randomUUID(), "Título válido", "Descrição.",
                    status, IssuePriority.LOW, now.plusDays(10), null, now, now
            );

            assertEquals(status, dto.status());
        }
    }

    @Test
    @DisplayName("should reflect all IssuePriority values correctly")
    void shouldReflectAllIssuePriorityValuesCorrectly() {
        for (IssuePriority priority : IssuePriority.values()) {
            IssueResponseDTO dto = new IssueResponseDTO(
                    UUID.randomUUID(), UUID.randomUUID(), "item_001", "Título snapshot",
                    UUID.randomUUID(), "Título válido", "Descrição.",
                    IssueStatus.OPEN, priority, now.plusDays(10), null, now, now
            );

            assertEquals(priority, dto.priority());
        }
    }

    @Test
    @DisplayName("should correctly store itemKey and itemTitleSnapshot independently")
    void shouldCorrectlyStoreItemKeyAndItemTitleSnapshotIndependently() {
        IssueResponseDTO dto = new IssueResponseDTO(
                UUID.randomUUID(), UUID.randomUUID(),
                "item_999",
                "Extintor fora do prazo de validade",
                UUID.randomUUID(), "Extintor vencido", "Extintor com validade expirada.",
                IssueStatus.IN_PROGRESS, IssuePriority.CRITICAL,
                now.plusDays(2), null, now, now
        );

        assertEquals("item_999", dto.itemKey());
        assertEquals("Extintor fora do prazo de validade", dto.itemTitleSnapshot());
    }
}