package com.portal.conecta.checklist.module.checklist.application.usecase.execution.query;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
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
 * apenas execuções com status {@link ChecklistExecutionStatus#SUBMITTED}.</p>
 */
@Service
@RequiredArgsConstructor
public class ListChecklistHistoryByClassUseCase {

    private final ChecklistExecutionRepository repository;
    private final RequestContextProvider contextProvider;

    /**
     * Busca o histórico de execuções submetidas para uma turma.
     *
     * @param classId identificador único da turma.
     * @return página de execuções submetidas da turma.
     * @throws AccessDeniedException quando o usuário atual não possui permissão para consultar a turma.
     */
    public Page<ChecklistExecution> execute(UUID classId, Pageable pageable) {
        var currentUser = contextProvider.getRequestContext();

        if (!currentUser.canManageChecklistTemplates()
                && !currentUser.canOperateChecklistExecutionForClass(classId)) {
            throw new AccessDeniedException(
                    "Usuario nao tem permissao para consultar o historico desta turma."
            );
        }

        return repository.findByClassIdAndStatusOrderBySubmittedAtDesc(
                classId,
                ChecklistExecutionStatus.SUBMITTED,
                pageable
        );


    }
}
