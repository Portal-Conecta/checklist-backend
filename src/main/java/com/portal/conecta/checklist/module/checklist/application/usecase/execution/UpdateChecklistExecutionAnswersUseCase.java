package com.portal.conecta.checklist.module.checklist.application.usecase.execution;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Caso de uso responsável por atualizar as respostas de um checklist já submetido.
 * Orquestra as regras de negócio de segurança, validação de estado, cálculo de score e geração de pendências adicionais.
 */
@Service
@RequiredArgsConstructor
public class UpdateChecklistExecutionAnswersUseCase {

    private final ChecklistExecutionRepository repository;
    private final ChecklistExecutionMapper executionMapper;
    private final RequestContextProvider contextProvider;
    private final ChecklistExecutionScoringService scoringService;
    private final ChecklistIssueService issueService;
    private final ObjectMapper objectMapper;

    /**
     * Executa o fluxo de edição das respostas de um determinado checklist.
     *
     * @param executionId o identificador único da execução do checklist.
     * @param request     o DTO contendo a lista atualizada com as novas respostas.
     * @return            a entidade {@link ChecklistExecution} atualizada e persistida.
     * @throws EntityNotFoundException se a execução informada não for localizada no banco de dados.
     * @throws AccessDeniedException   se o usuário atual não possuir permissões administrativas ou de docência na turma correspondente.
     * @throws IllegalStateException   se o status atual da execução for diferente de SUBMITTED.
     */
    @Transactional
    public ChecklistExecution execute(UUID executionId, ChecklistExecutionSubmitDTO request) {
        ChecklistExecution execution = repository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execucao de checklist nao encontrada: " + executionId));

        var currentUser = contextProvider.getRequestContext();

        if (!currentUser.canManageChecklistTemplates()
                && !currentUser.canOperateChecklistExecutionForClass(execution.getClassId())) {
            throw new AccessDeniedException("Usuario nao tem permissao para editar esta execucao de checklist.");
        }

        if (execution.getStatus() != ChecklistExecutionStatus.SUBMITTED) {
            throw new IllegalStateException("Somente checklists que foram enviados podem ser editados.");
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
     * Transforma a árvore hierárquica do schema (seções -> itens) em um mapa linear indexado pela chave única do item.
     */
    private Map<String, ChecklistItemDTO> itemsByKey(ChecklistSchemaDTO schema) {
        return schema.sections().stream()
                .flatMap(section -> section.items().stream())
                .collect(Collectors.toMap(ChecklistItemDTO::key, Function.identity(), (first, duplicated) -> first, LinkedHashMap::new));
    }
}
