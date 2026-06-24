package com.portal.conecta.checklist.module.checklist.application.usecase.execution;

import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchChecklistExecutionsByClassOrRepresentativeUseCase {

    private final ChecklistExecutionRepository repository;
    private final RequestContextProvider contextProvider;

    @Transactional(readOnly = true)
    public List<ChecklistExecution> execute(String query) {
        validate(query);

        var currentUser = contextProvider.getRequestContext();

        if (!currentUser.canManageChecklistTemplates()) {
            throw new AccessDeniedException("Acesso negado: apenas gestores podem buscar execucoes de checklist.");
        }

        return repository.searchByClassOrRepresentativeName(query.trim());
    }

    private void validate(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("O termo de busca nao pode ser vazio.");
        }
        if (query.trim().length() < 2) {
            throw new IllegalArgumentException("O termo de busca deve ter ao menos 2 caracteres.");
        }
    }
}
