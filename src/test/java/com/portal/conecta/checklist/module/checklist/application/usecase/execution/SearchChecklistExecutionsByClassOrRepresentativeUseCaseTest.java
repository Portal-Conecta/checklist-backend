package com.portal.conecta.checklist.module.checklist.application.usecase.execution;

import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.shared.context.ContextClass;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.context.TypeUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SearchChecklistExecutionsByClassOrRepresentativeUseCaseTest {

    private final ChecklistExecutionRepository repository = mock(ChecklistExecutionRepository.class);
    private final RequestContextProvider contextProvider = mock(RequestContextProvider.class);
    private final SearchChecklistExecutionsByClassOrRepresentativeUseCase useCase =
            new SearchChecklistExecutionsByClassOrRepresentativeUseCase(repository, contextProvider);

    @Test
    @DisplayName("deve retornar execucoes quando query valida e usuario gestor")
    void deveRetornarExecucoesQuandoQueryValidaEUsuarioGestor() {
        String query = "João";
        List<ChecklistExecution> expected = List.of(mock(ChecklistExecution.class));

        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(repository.searchByClassOrRepresentativeName("João")).thenReturn(expected);

        List<ChecklistExecution> result = useCase.execute(query);

        assertSame(expected, result);
        verify(repository).searchByClassOrRepresentativeName("João");
    }

    @Test
    @DisplayName("deve aplicar trim na query antes de pesquisar")
    void deveAplicarTrimNaQueryAntesdePesquisar() {
        String query = "  Turma A  ";
        List<ChecklistExecution> expected = List.of(mock(ChecklistExecution.class));

        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(repository.searchByClassOrRepresentativeName("Turma A")).thenReturn(expected);

        List<ChecklistExecution> result = useCase.execute(query);

        assertSame(expected, result);
        verify(repository).searchByClassOrRepresentativeName("Turma A");
    }

    @Test
    @DisplayName("deve rejeitar query nula")
    void deveRejeitarQueryNula() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(null));

        assertEquals("O termo de busca nao pode ser vazio.", ex.getMessage());
        verify(repository, never()).searchByClassOrRepresentativeName(null);
    }

    @Test
    @DisplayName("deve rejeitar query em branco")
    void deveRejeitarQueryEmBranco() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("   "));

        assertEquals("O termo de busca nao pode ser vazio.", ex.getMessage());
        verify(repository, never()).searchByClassOrRepresentativeName(null);
    }

    @Test
    @DisplayName("deve rejeitar query com apenas um caractere")
    void deveRejeitarQueryComApenasUmCaractere() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("A"));

        assertEquals("O termo de busca deve ter ao menos 2 caracteres.", ex.getMessage());
        verify(repository, never()).searchByClassOrRepresentativeName(null);
    }

    @Test
    @DisplayName("deve aceitar query com exatamente dois caracteres")
    void deveAceitarQueryComExatamenteDoisCaracteres() {
        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(repository.searchByClassOrRepresentativeName("AB")).thenReturn(List.of());

        List<ChecklistExecution> result = useCase.execute("AB");

        assertEquals(List.of(), result);
    }

    @Test
    @DisplayName("deve rejeitar acesso de representante")
    void deveRejeitarAcessoDeRepresentante() {
        when(contextProvider.getRequestContext()).thenReturn(representative());

        assertThrows(AccessDeniedException.class, () -> useCase.execute("Turma"));

        verify(repository, never()).searchByClassOrRepresentativeName(null);
    }

    @Test
    @DisplayName("deve rejeitar acesso de professor")
    void deveRejeitarAcessoDeProfessor() {
        when(contextProvider.getRequestContext()).thenReturn(teacher());

        assertThrows(AccessDeniedException.class, () -> useCase.execute("Turma"));

        verify(repository, never()).searchByClassOrRepresentativeName(null);
    }

    @Test
    @DisplayName("deve permitir acesso de usuario WEG")
    void devePermitirAcessoDeUsuarioWeg() {
        when(contextProvider.getRequestContext()).thenReturn(weg());
        when(repository.searchByClassOrRepresentativeName("Turma")).thenReturn(List.of());

        List<ChecklistExecution> result = useCase.execute("Turma");

        assertEquals(List.of(), result);
        verify(repository).searchByClassOrRepresentativeName("Turma");
    }

    // --- helpers ---

    private RequestContext senai() {
        return new RequestContext(UUID.randomUUID(), TypeUser.SENAI);
    }

    private RequestContext weg() {
        return new RequestContext(UUID.randomUUID(), TypeUser.WEG);
    }

    private RequestContext representative() {
        return new RequestContext(
                UUID.randomUUID(),
                TypeUser.REPRESENTATIVE,
                List.of(new ContextClass(UUID.randomUUID(), "REPRESENTATIVE"))
        );
    }

    private RequestContext teacher() {
        return new RequestContext(
                UUID.randomUUID(),
                TypeUser.TEACHER,
                List.of(new ContextClass(UUID.randomUUID(), "TEACHER"))
        );
    }
}
