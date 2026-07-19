package com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.cancel;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.application.port.out.persistence.ChecklistExecutionRepositoryPort;
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
 * Aceita cancelar execuções em {@code DRAFT} ou {@code SUBMITTED}. Cancelar um
 * DRAFT é o único jeito de descartar um rascunho criado por engano (turma
 * errada, tentativa duplicada) — sem isso, ele fica travado para sempre e
 * bloqueia a criação de um novo checklist para a mesma turma/sala/período/
 * tipo/dia, já que o índice único ignora apenas status {@code CANCELED}.
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
     * 2. Permissão do usuário (operador da turma ou gestor)
     * 3. Status {@code SUBMITTED}
     * </p>
     * <p>
     * A permissão é validada antes do estado para não vazar informação de estado
     * a quem não tem acesso: usuário sem permissão recebe sempre 403, sem conseguir
     * distinguir se a execução está em DRAFT, SUBMITTED ou CANCELED.
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
     * @throws AccessDeniedException    se o usuário atual não tiver permissão para cancelar esta execução.
     * @throws IllegalArgumentException se o checklist já estiver {@code CANCELED}.
     */
    @Transactional
    public ChecklistExecution execute(UUID executionId) {
        ChecklistExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execucao de checklist nao encontrada"));

        var currentUser = contextProvider.getRequestContext();

        if (!currentUser.canCancelChecklistExecution(execution.getClassId())) {
            throw new AccessDeniedException("Usuario nao tem permissao para cancelar esta execucao de checklist.");
        }

        if (execution.getStatus() != ChecklistExecutionStatus.SUBMITTED
                && execution.getStatus() != ChecklistExecutionStatus.DRAFT) {
            throw new IllegalArgumentException("Somente checklist em rascunho ou enviados podem ser cancelados");
        }

        execution.setStatus(ChecklistExecutionStatus.CANCELED);
        execution.setCanceledBy(currentUser.userId());

        return executionRepository.save(execution);
    }
}
