package com.portal.conecta.checklist.module.checklist.application.usecase.execution.query;

import com.portal.conecta.checklist.module.checklist.application.port.out.persistence.ChecklistExecutionRepositoryPort;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FindChecklistExecutionByIdUseCaseTest {

    private final ChecklistExecutionRepositoryPort repositoryPort = mock(ChecklistExecutionRepositoryPort.class);
    private final RequestContextProvider contextProvider = mock(RequestContextProvider.class);
    private final FindChecklistExecutionByIdUseCase useCase = new FindChecklistExecutionByIdUseCase(repositoryPort, contextProvider);

    @Test
    @DisplayName("deve retornar execution ao buscar por ID existente e com permissao")
    void deveRetornarExecutionAoBuscarPorIdExistenteEPermissao() {
        UUID id = UUID.randomUUID();
        ChecklistExecution expectedExecution = mock(ChecklistExecution.class);
        RequestContext context = mock(RequestContext.class);

        when(repositoryPort.findById(id)).thenReturn(Optional.of(expectedExecution));
        when(contextProvider.getRequestContext()).thenReturn(context);
        when(context.canManageChecklistTemplates()).thenReturn(true);

        ChecklistExecution actualExecution = useCase.execute(id);

        assertSame(expectedExecution, actualExecution);
        verify(repositoryPort).findById(id);
    }
    
    @Test
    @DisplayName("deve lancar AccessDeniedException ao buscar por ID existente sem permissao")
    void deveLancarAccessDeniedExceptionAoBuscarPorIdExistenteSemPermissao() {
        UUID id = UUID.randomUUID();
        ChecklistExecution expectedExecution = mock(ChecklistExecution.class);
        RequestContext context = mock(RequestContext.class);

        when(repositoryPort.findById(id)).thenReturn(Optional.of(expectedExecution));
        when(expectedExecution.getClassId()).thenReturn(UUID.randomUUID());
        when(contextProvider.getRequestContext()).thenReturn(context);
        when(context.canManageChecklistTemplates()).thenReturn(false);
        when(context.canOperateChecklistExecutionForClass(any())).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> useCase.execute(id));
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
