package com.portal.conecta.checklist.modules.checklist.application.usecase.execution.query;

import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistExecutionRepositoryPort;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistExecution;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FindChecklistExecutionByIdUseCaseTest {

    private final ChecklistExecutionRepositoryPort repositoryPort = mock(ChecklistExecutionRepositoryPort.class);
    private final FindChecklistExecutionByIdUseCase useCase = new FindChecklistExecutionByIdUseCase(repositoryPort);

    @Test
    @DisplayName("deve retornar execution ao buscar por ID existente")
    void deveRetornarExecutionAoBuscarPorIdExistente() {
        UUID id = UUID.randomUUID();
        ChecklistExecution expectedExecution = mock(ChecklistExecution.class);

        when(repositoryPort.findById(id)).thenReturn(Optional.of(expectedExecution));

        ChecklistExecution actualExecution = useCase.execute(id);

        assertSame(expectedExecution, actualExecution);
        verify(repositoryPort).findById(id);
    }

    @Test
    @DisplayName("deve lancar EntityNotFoundException ao buscar por ID inexistente")
    void deveLancarEntityNotFoundExceptionAoBuscarPorIdInexistente() {
        UUID id = UUID.randomUUID();

        when(repositoryPort.findById(id)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> useCase.execute(id));
        assertEquals("Execução de checklist não encontrada para o ID informado", exception.getMessage());

        verify(repositoryPort).findById(id);
    }
}
