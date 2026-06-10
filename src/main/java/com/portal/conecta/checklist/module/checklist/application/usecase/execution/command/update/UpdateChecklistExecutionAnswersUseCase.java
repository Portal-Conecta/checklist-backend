package com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.update;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.core.ChecklistExecutionScoringService;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.core.ChecklistIssueService;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionSubmitDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistItemDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSchemaDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import java.util.LinkedHashMap;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Caso de Uso responsável por atualizar as respostas de um Checklist já submetido.
 * Orquestra as regras de negócio de segurança, validação de estado, cálculo de score e geração de pendências adicionais.
 */
@Service
@RequiredArgsConstructor
public class UpdateChecklistExecutionAnswersUseCase {


    private final ChecklistExecutionRepository repository;
    private final ChecklistExecutionMapper executionMapper;
    private final RequestContextProvider contextProvider;
    private final ChecklistExecutionScoringService scoringService;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private  final ChecklistIssueService issueService;


    /**
     * Executa o fluxo de re-submissão e edição das respostas de um determinado checklist.
     *
     * @param executionId O ID identificador único da execução do checklist.
     * @param request     O DTO contendo a lista atualizada com as novas respostas.
     * @return            A entidade {@link ChecklistExecution} atualizada e persistida.
     * @throws EntityNotFoundException  Se a execução informada não for localizada no banco de dados.
     * @throws AccessDeniedException    Se o usuário atual não possuir permissões administrativas ou de docência na turma correspondente.
     * @throws IllegalStateException    Se o status atual da execução for diferente de SUBMITTED.
     */

    @Transactional
    public ChecklistExecution execute(UUID executionId, ChecklistExecutionSubmitDTO request){

        ChecklistExecution execution = repository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execucao de checklist não encontrada: " + executionId));


        var currentUser = contextProvider.getRequestContext();

        if(!currentUser.canManageChecklistTemplates()
                && !currentUser.canOperateChecklistExecutionForClass(execution.getClassId())){
                throw new AccessDeniedException("Usuario não tem permissao para editar esta execucao de checklist.");
        }

        if (execution.getStatus() != ChecklistExecutionStatus.SUBMITTED){
            throw new IllegalStateException("Somente checklists que foram enviadas podem ser editadas.");

        }

        ChecklistSchemaDTO schema = objectMapper.convertValue(
                execution.getChecklistTemplate().getSchemaJson(),
                ChecklistSchemaDTO.class
        );

        Map<String, ChecklistItemDTO> itemsByKey = itemsByKey(schema);

        issueService.createIssuesForNonCompliantAnswers(execution, request.answers(), itemsByKey);

        execution.setAnswersJson(executionMapper.toAnswersJson(request));
        execution.setComplianceScore(scoringService.calculateComplianceScore(request.answers()));


        return repository.save(execution);


    }
    /**
     * Transforma a árvore hierárquica do Schema (Seções -> Itens) em um mapa linear indexado pela chave única do item.
     */
    private Map<String, ChecklistItemDTO> itemsByKey(ChecklistSchemaDTO schema) {
        return schema.sections().stream()
                .flatMap(section -> section.items().stream())
                .collect(Collectors.toMap(ChecklistItemDTO::key, Function.identity(), (f, d) -> f, LinkedHashMap::new));
    }


}
