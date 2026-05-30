package com.portal.conecta.checklist.module.checklist.application.facade;


import com.portal.conecta.checklist.module.checklist.application.usecase.execution.CancelChecklistExecutionUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.CreateChecklistExecutionUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.ListChecklistHistoryByClassUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.SubmitChecklistExecutionUseCase;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionDraftCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionSubmitDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistExecutionHistoryDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistExecutionResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Camada de Fachada (Facade) para orquestrar as operações relacionadas à Execução de Checklists.
 * <p>
 * Atua como um intermediário entre os controladores (presentation) e os casos de uso (application),
 * abstraindo a complexidade das regras de negócio e sendo responsável por delegar as chamadas aos
 * UseCases adequados, bem como mapear as entidades de domínio de volta para DTOs de resposta.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class ChecklistExecutionFacade {

private final CreateChecklistExecutionUseCase createChecklistExecutionUseCase;
private final SubmitChecklistExecutionUseCase submitChecklistExecutionUseCase;
private final ChecklistExecutionMapper executionMapper;
private final CancelChecklistExecutionUseCase cancelChecklistExecutionUseCase;
private final ListChecklistHistoryByClassUseCase listChecklistHistoryByClassUseCase;

    /**
     * Inicia a criação de um novo rascunho de execução de checklist.
     * <p>
     * Delega a lógica de criação para o {@link CreateChecklistExecutionUseCase} e converte
     * a entidade resultante em um DTO para a camada de apresentação.
     * </p>
     *
     * @param request o DTO contendo os dados necessários para criar o rascunho.
     * @return um {@link ChecklistExecutionResponseDTO} representando o checklist recém-criado.
     */

    public ChecklistExecutionResponseDTO createDTO(ChecklistExecutionDraftCreateDTO request){

        ChecklistExecution execution = createChecklistExecutionUseCase.execute(request);
        return  executionMapper.toResponse(execution);
    }
    /**
     * Submete as respostas de uma execução de checklist em formato de rascunho.
     * <p>
     * Delega a validação e o processamento da submissão para o {@link SubmitChecklistExecutionUseCase}
     * e converte a entidade submetida em um DTO de resposta.
     * </p>
     *
     * @param executionId o identificador único da execução do checklist.
     * @param request     o DTO contendo as respostas enviadas pelo usuário.
     * @return um {@link ChecklistExecutionResponseDTO} contendo os dados do checklist atualizado.
     */

    public ChecklistExecutionResponseDTO submit(UUID executionId, ChecklistExecutionSubmitDTO request) {
        ChecklistExecution execution = submitChecklistExecutionUseCase.execute(executionId, request);
        return executionMapper.toResponse(execution);
    }

    /**
     * Cancela uma execução de checklist previamente submetida.
     * <p>
     * Delega a verificação de regras de cancelamento para o {@link CancelChecklistExecutionUseCase}
     * e retorna a entidade cancelada mapeada em um DTO.
     * </p>
     *
     * @param executionId o identificador único da execução do checklist a ser cancelada.
     * @return um {@link ChecklistExecutionResponseDTO} contendo o novo status da execução.
     */

    public ChecklistExecutionResponseDTO cancel(UUID executionId){
        ChecklistExecution execution = cancelChecklistExecutionUseCase.execute(executionId);
        return  executionMapper.toResponse(execution);
    }

    public List<ChecklistExecutionHistoryDTO> listHistoryByClass(UUID classId) {
        return listChecklistHistoryByClassUseCase.execute(classId);
    }
}
