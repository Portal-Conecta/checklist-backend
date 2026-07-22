package com.portal.conecta.checklist.module.checklist.application.usecase.execution.query;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistCategory;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.application.port.out.persistence.ChecklistExecutionRepositoryPort;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Caso de uso responsável por consultar o histórico de checklists submetidos por turma.
 *
 * <p>Valida se o usuário atual possui permissão para acessar a turma informada e retorna
 * apenas execuções com status {@link ChecklistExecutionStatus#SUBMITTED}.
 * Aceita filtro opcional por {@link ChecklistCategory}.</p>
 */
@Service
@RequiredArgsConstructor
public class ListChecklistHistoryByClassUseCase {

    private final ChecklistExecutionRepositoryPort repository;
    private final RequestContextProvider contextProvider;

    public Page<ChecklistExecution> execute(UUID classId, Pageable pageable) {
        return execute(classId, pageable, null);
    }

    public Page<ChecklistExecution> execute(UUID classId, Pageable pageable, ChecklistCategory category) {
        var currentUser = contextProvider.getRequestContext();

        if (!currentUser.canManageChecklistTemplates()
                && !currentUser.canOperateChecklistExecutionForClass(classId)) {
            throw new AccessDeniedException(
                    "Usuario nao tem permissao para consultar o historico desta turma."
            );
        }

        if (category == null) {
            return repository.findByClassIdAndStatusOrderBySubmittedAtDesc(
                    classId,
                    ChecklistExecutionStatus.SUBMITTED,
                    pageable
            );
        }

        return repository.findByClassIdAndStatusAndCategoryOrderBySubmittedAtDesc(
                classId,
                ChecklistExecutionStatus.SUBMITTED,
                category,
                pageable
        );
    }
}
