package com.portal.conecta.checklist.unit.checklist.application.usecase.execution.query;

import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistExecutionRepositoryPort;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.query.ChecklistExecutionFilter;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.query.ListChecklistExecutionsUseCase;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistCategory;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.shared.context.ClassRole;
import com.portal.conecta.checklist.shared.context.ContextClass;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.context.TypeUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ListChecklistExecutionsUseCaseTest {

    private final ChecklistExecutionRepositoryPort repositoryPort = mock(ChecklistExecutionRepositoryPort.class);
    private final RequestContextProvider contextProvider = mock(RequestContextProvider.class);
    private final ListChecklistExecutionsUseCase useCase = new ListChecklistExecutionsUseCase(repositoryPort, contextProvider);

    @Test
    @DisplayName("deve retornar execucoes ao listar sem filtros como gestor")
    void deveRetornarExecucoesAoListarSemFiltrosComoGestor() {
        Pageable pageable = PageRequest.of(0, 20);
        RequestContext context = new RequestContext(UUID.randomUUID(), TypeUser.SENAI, List.of());
        Page<ChecklistExecution> expectedPage = new PageImpl<>(List.of());

        when(contextProvider.getRequestContext()).thenReturn(context);
        when(repositoryPort.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        ChecklistExecutionFilter filter = new ChecklistExecutionFilter(null, null, null, null, null);
        Page<ChecklistExecution> actualPage = useCase.execute(filter, pageable);

        assertSame(expectedPage, actualPage);
        verify(repositoryPort).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("deve retornar execucoes ao listar sem filtros como usuario comum com turmas")
    void deveRetornarExecucoesAoListarSemFiltrosComoUsuarioComum() {
        Pageable pageable = PageRequest.of(0, 20);
        UUID classId = UUID.randomUUID();
        ContextClass contextClass = new ContextClass(classId, ClassRole.REPRESENTATIVE);
        RequestContext context = new RequestContext(UUID.randomUUID(), TypeUser.STUDENT, List.of(contextClass));
        Page<ChecklistExecution> expectedPage = new PageImpl<>(List.of());

        when(contextProvider.getRequestContext()).thenReturn(context);
        when(repositoryPort.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        ChecklistExecutionFilter filter = new ChecklistExecutionFilter(null, null, null, null, null);
        Page<ChecklistExecution> actualPage = useCase.execute(filter, pageable);

        assertSame(expectedPage, actualPage);
        verify(repositoryPort).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("deve retornar pagina vazia sem interagir com o banco se o usuario comum nao tiver turmas")
    void deveRetornarVazioAoListarSemFiltrosEUsuarioSemTurmas() {
        Pageable pageable = PageRequest.of(0, 20);
        RequestContext context = new RequestContext(UUID.randomUUID(), TypeUser.STUDENT, List.of());

        when(contextProvider.getRequestContext()).thenReturn(context);

        ChecklistExecutionFilter filter = new ChecklistExecutionFilter(null, null, null, null, null);
        Page<ChecklistExecution> actualPage = useCase.execute(filter, pageable);

        assertTrue(actualPage.isEmpty());
        verifyNoInteractions(repositoryPort);
    }

    @Test
    @DisplayName("deve retornar execucoes ao filtrar por classId pertencente ao usuario comum")
    void deveRetornarExecucoesAoFiltrarPorClassIdValidoComoUsuarioComum() {
        Pageable pageable = PageRequest.of(0, 20);
        UUID classId = UUID.randomUUID();
        ContextClass contextClass = new ContextClass(classId, ClassRole.REPRESENTATIVE);
        RequestContext context = new RequestContext(UUID.randomUUID(), TypeUser.STUDENT, List.of(contextClass));
        Page<ChecklistExecution> expectedPage = new PageImpl<>(List.of());

        when(contextProvider.getRequestContext()).thenReturn(context);
        when(repositoryPort.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        ChecklistExecutionFilter filter = new ChecklistExecutionFilter(classId, null, null, null, null);
        Page<ChecklistExecution> actualPage = useCase.execute(filter, pageable);

        assertSame(expectedPage, actualPage);
        verify(repositoryPort).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("deve retornar pagina vazia sem interagir com o banco se o usuario comum filtrar por classId que nao possui acesso")
    void deveRetornarVazioAoFiltrarPorClassIdInvalidoComoUsuarioComum() {
        Pageable pageable = PageRequest.of(0, 20);
        UUID classIdUser = UUID.randomUUID();
        UUID classIdRequested = UUID.randomUUID();
        ContextClass contextClass = new ContextClass(classIdUser, ClassRole.REPRESENTATIVE);
        RequestContext context = new RequestContext(UUID.randomUUID(), TypeUser.STUDENT, List.of(contextClass));

        when(contextProvider.getRequestContext()).thenReturn(context);

        ChecklistExecutionFilter filter = new ChecklistExecutionFilter(classIdRequested, null, null, null, null);
        Page<ChecklistExecution> actualPage = useCase.execute(filter, pageable);

        assertTrue(actualPage.isEmpty());
        verifyNoInteractions(repositoryPort);
    }

    @Test
    @DisplayName("deve retornar execucoes ao filtrar com todos os filtros como gestor")
    void deveRetornarExecucoesAoFiltrarComTodosOsCamposComoGestor() {
        Pageable pageable = PageRequest.of(0, 20);
        RequestContext context = new RequestContext(UUID.randomUUID(), TypeUser.SENAI, List.of());
        Page<ChecklistExecution> expectedPage = new PageImpl<>(List.of());

        when(contextProvider.getRequestContext()).thenReturn(context);
        when(repositoryPort.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        ChecklistExecutionFilter filter = new ChecklistExecutionFilter(
                UUID.randomUUID(),
                UUID.randomUUID(),
                ChecklistCategory.ELETRONICOS,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );
        Page<ChecklistExecution> actualPage = useCase.execute(filter, pageable);

        assertSame(expectedPage, actualPage);
        verify(repositoryPort).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("deve retornar execucoes ao filtrar por category como gestor")
    void deveRetornarExecucoesAoFiltrarPorCategoryComoGestor() {
        Pageable pageable = PageRequest.of(0, 20);
        RequestContext context = new RequestContext(UUID.randomUUID(), TypeUser.SENAI, List.of());
        Page<ChecklistExecution> expectedPage = new PageImpl<>(List.of());

        when(contextProvider.getRequestContext()).thenReturn(context);
        when(repositoryPort.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        ChecklistExecutionFilter filter = new ChecklistExecutionFilter(null, null, ChecklistCategory.MOVEIS, null, null);
        Page<ChecklistExecution> actualPage = useCase.execute(filter, pageable);

        assertSame(expectedPage, actualPage);
        verify(repositoryPort).findAll(any(Specification.class), eq(pageable));
    }
}
