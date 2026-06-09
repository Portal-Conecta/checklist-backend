package com.portal.conecta.checklist.module.checklist.application.facade;


import com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.cancel.CancelChecklistExecutionCommandUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.create.CreateChecklistExecutionUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.update.UpdateChecklistExecutionAnswersUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.query.ListChecklistHistoryByClassUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.submit.SubmitChecklistExecutionUseCase;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionDraftCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionSubmitDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistExecutionHistoryDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistExecutionResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Fachada responsável por orquestrar as operações de execução de checklists.
 *
 * <p>Centraliza a comunicação entre a camada de apresentação e os casos de uso de execução,
 * delegando regras de negócio e convertendo entidades de domínio em DTOs de resposta.</p>
 */
@Component
@RequiredArgsConstructor
public class ChecklistExecutionFacade {

    private final CreateChecklistExecutionUseCase createChecklistExecutionUseCase;
    private final SubmitChecklistExecutionUseCase submitChecklistExecutionUseCase;
    private final ChecklistExecutionMapper executionMapper;
    private final CancelChecklistExecutionCommandUseCase cancelChecklistExecutionUseCase;
    private final ListChecklistHistoryByClassUseCase listChecklistHistoryByClassUseCase;
    private final UpdateChecklistExecutionAnswersUseCase updateChecklistExecutionAnswersUseCase;




    /**
     * Cria uma execução de checklist em formato de rascunho.
     *
     * @param request dados necessários para criação do rascunho.
     * @return execução criada, já convertida para DTO de resposta.
     */
    public ChecklistExecutionResponseDTO createDTO(ChecklistExecutionDraftCreateDTO request) {
        ChecklistExecution execution = createChecklistExecutionUseCase.execute(request);
        return executionMapper.toResponse(execution);
    }

    /**
     * Submete as respostas de uma execução de checklist.
     *
     * <p>Delega validação e processamento para {@link SubmitChecklistExecutionUseCase}
     * e retorna a execução atualizada em formato de resposta.</p>
     *
     * @param executionId identificador único da execução do checklist.
     * @param request respostas enviadas para submissão.
     * @return execução submetida, já convertida para DTO de resposta.
     */
    public ChecklistExecutionResponseDTO submit(UUID executionId, ChecklistExecutionSubmitDTO request) {
        ChecklistExecution execution = submitChecklistExecutionUseCase.execute(executionId, request);
        return executionMapper.toResponse(execution);
    }

    /**
     * Cancela uma execução de checklist.
     *
     * <p>Delega as regras de cancelamento para {@link CancelChecklistExecutionCommandUseCase}
     * e retorna a execução com o status atualizado.</p>
     *
     * @param executionId identificador único da execução do checklist a ser cancelada.
     * @return execução cancelada, já convertida para DTO de resposta.
     */
    public ChecklistExecutionResponseDTO cancel(UUID executionId) {
        ChecklistExecution execution = cancelChecklistExecutionUseCase.execute(executionId);
        return executionMapper.toResponse(execution);
    }

    /**
     * Lista o histórico de execuções submetidas para uma turma.
     *
     * @param classId identificador único da turma consultada.
     * @return lista de execuções históricas da turma, ordenada conforme regra do caso de uso.
     */
    public Page<ChecklistExecutionHistoryDTO> listHistoryByClass(UUID classId, Pageable pageable) {
        return executionMapper.toPageHistory(
                listChecklistHistoryByClassUseCase.execute(classId, pageable)
        );
    }
    public ChecklistExecutionResponseDTO updateAnswers(UUID executionId, ChecklistExecutionSubmitDTO request) {
        return executionMapper.toResponse(
                updateChecklistExecutionAnswersUseCase.execute(executionId, request)
        );

    }
}
