package com.portal.conecta.checklist.module.checklist.application.usecase.execution.cancel;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
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


    private final ChecklistExecutionRepository executionRepository;
    private final RequestContextProvider contextProvider;


    /**
     * Executa o cancelamento de um checklist.
     * <p>
     * O processo realiza as seguintes validações:
     * 1. Verifica se a execução do checklist existe na base de dados.
     * 2. Verifica se o usuário atual tem permissão para cancelar o checklist (validando
     * o criador original e a turma vinculada).
     * 3. Garante que apenas checklists com status {@code SUBMITTED} (enviados) possam ser cancelados.
     * </p>
     * Se todas as regras forem atendidas, o status da execução é alterado para {@code CANCELED}.
     *
     * @param executionId o identificador único da execução do checklist que será cancelada.
     * @return a entidade {@link ChecklistExecution} atualizada e persistida com o novo status.
     * @throws EntityNotFoundException  se a execução do checklist não for encontrada.
     * @throws AccessDeniedException    se o usuário atual não tiver permissão para cancelar esta execução.
     * @throws IllegalArgumentException se o checklist não estiver no status {@code SUBMITTED}.
     */


    @Transactional
    public ChecklistExecution execute(UUID executionId){
        ChecklistExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execucao de checklist nao encontrada"));

        var currentUser = contextProvider.getRequestContext();

        if (!currentUser.canCancelChecklistExecution(execution.getUserId(), execution.getClassId())) {
            throw new AccessDeniedException("Usuario nao tem permissao para cancelar esta execucao de checklist.");
        }

        if(execution.getStatus() != ChecklistExecutionStatus.SUBMITTED){
            throw new IllegalArgumentException("Somente checklist enviados podem ser cancelados");
        }
        execution.setStatus(ChecklistExecutionStatus.CANCELED);

        return executionRepository.save(execution);


    }


}
