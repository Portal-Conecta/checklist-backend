package com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.cancel;

import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistExecutionRepositoryPort;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso responsável pelo cancelamento de uma execução de checklist.
 * <p>
 * Este serviço garante que apenas checklists que já foram enviados (SUBMITTED)
 * possam ser cancelados e valida se o usuário solicitante possui os privilégios
 * necessários para realizar a operação.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class CancelChecklistExecutionUseCase {

    private final ChecklistExecutionRepositoryPort executionRepository;
    private final RequestContextProvider contextProvider;

    /**
     * Executa o cancelamento de um checklist.
     * <p>
     * Ordem das validações:
     * 1. Existência da execução
     * 2. Status {@code SUBMITTED}
     * 3. Permissão do usuário (operador da turma ou gestor)
     * </p>
     * <p>
     * Não há limite de quantidade de SUBMITTED neste fluxo: cancelar deve sempre
     * ser permitido quando status e permissão forem válidos. Um teto de execuções
     * ativas, se o negócio formalizar, deve ser aplicado em create/submit — não no cancel.
     * </p>
     *
     * @param executionId o identificador único da execução do checklist que será cancelada.
     * @return a entidade {@link ChecklistExecution} atualizada e persistida com o novo status.
     * @throws EntityNotFoundException  se a execução do checklist não for encontrada.
     * @throws IllegalArgumentException se o checklist não estiver no status {@code SUBMITTED}.
     * @throws AccessDeniedException    se o usuário atual não tiver permissão para cancelar esta execução.
     */
    @Transactional
    public ChecklistExecution execute(UUID executionId) {
        ChecklistExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execucao de checklist nao encontrada"));

        if (execution.getStatus() != ChecklistExecutionStatus.SUBMITTED) {
            throw new IllegalArgumentException("Somente checklist enviados podem ser cancelados");
        }

        var currentUser = contextProvider.getRequestContext();

        if (!currentUser.canCancelChecklistExecution(execution.getClassId())) {
            throw new AccessDeniedException("Usuario nao tem permissao para cancelar esta execucao de checklist.");
        }

        execution.setStatus(ChecklistExecutionStatus.CANCELED);
        execution.setCanceledBy(currentUser.userId());

        return executionRepository.save(execution);
    }
}
