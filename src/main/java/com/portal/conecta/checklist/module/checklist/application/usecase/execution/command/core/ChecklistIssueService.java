package com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.core;

import com.portal.conecta.checklist.module.checklist.domain.enums.ConformityAnswerValue;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.UserReference;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistAnswerRequestDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistItemDTO;
import com.portal.conecta.checklist.module.issues.domain.enums.IssuePriority;
import com.portal.conecta.checklist.module.issues.domain.enums.IssueStatus;
import com.portal.conecta.checklist.module.issues.domain.model.ChecklistIssue;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChecklistIssueService {

    /**
     * Serviço responsável pelo gerenciamento de não-conformidades (Issues) originadas de execuções de checklists.
     */

    private static final int ISSUE_DUE_DAYS = 7;

    /**
     * Analisa as respostas fornecidas e cria novas pendências (Issues) para cada resposta marcada como 'NON_COMPLIANT'.
     * Possui inteligência para evitar a geração de pendências duplicadas para chaves de itens que já possuem issues associadas.
     *
     * @param execution   A entidade de execução do checklist atual que receberá as pendências.
     * @param answers     Lista de respostas enviadas na requisição.
     * @param itemsByKey  Mapa de mapeamento dos itens de schema originais para captura de metadados (como o título).
     */

    public void createIssuesForNonCompliantAnswers(
            ChecklistExecution execution,
            List<ChecklistAnswerRequestDTO> answers,
            Map<String, ChecklistItemDTO> itemsByKey
    ) {
        Instant dueAt = Instant.now().plusSeconds(ISSUE_DUE_DAYS * 24L * 60L * 60L);

        Set<String> existingIssueKeys = execution.getIssues() != null
                ? execution.getIssues().stream().map(ChecklistIssue::getItemKey).collect(Collectors.toSet())
                : Set.of();

        answers.stream()
                .filter(answer -> answer.value() == ConformityAnswerValue.NON_COMPLIANT)
                .filter(answer -> !existingIssueKeys.contains(answer.itemKey())) // Proteção contra duplicidade
                .forEach(answer -> {
                    ChecklistItemDTO item = itemsByKey.get(answer.itemKey());

                    if (item != null) {
                        execution.addIssue(ChecklistIssue.builder()
                                .assignedUserReference(new UserReference(execution.getUserId()))
                                .itemKey(answer.itemKey())
                                .itemTitleSnapshot(truncate(item.title(), 150))
                                .title(truncate("Pendencia: " + item.title(), 100))
                                .description(truncate(answer.observation(), 500))
                                .status(IssueStatus.OPEN)
                                .priority(IssuePriority.MEDIUM)
                                .dueAt(dueAt)
                                .build());
                    }
                });
    }
    /**
     * Método utilitário para garantir que os textos não estourem o limite de caracteres das colunas do banco de dados.
     */
    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}