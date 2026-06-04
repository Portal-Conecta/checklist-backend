package com.portal.conecta.checklist.module.checklist.application.usecase.execution;


import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListChecklistExecutionUseCase {

    private final ChecklistExecutionRepository executionRepository;
    private final RequestContextProvider contextProvider;

    @Transactional(readOnly = true)
    public Page<ChecklistExecution> execute(Pageable pageable) {
        var currentUser = contextProvider.getRequestContext();

        if (!currentUser.canAccessChecklistModule()) {
            throw new AccessDeniedException(
                    "Usuario não tem permissão para acessar o modulo Checklist."
            );
        }

        return executionRepository.findAll(pageable);
    }
}