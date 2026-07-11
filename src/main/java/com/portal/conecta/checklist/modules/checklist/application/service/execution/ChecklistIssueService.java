package com.portal.conecta.checklist.modules.checklist.application.service.execution;

import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.update.UpdateChecklistAnswerCommand;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ConformityAnswerValue;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.modules.checklist.domain.valueobject.UserReference;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistItem;
import com.portal.conecta.checklist.modules.checklist.issues.domain.enums.IssuePriority;
import com.portal.conecta.checklist.modules.checklist.issues.domain.enums.IssueStatus;
import com.portal.conecta.checklist.modules.checklist.issues.domain.model.ChecklistIssue;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Serviço responsável pelo gerenciamento de não-conformidades (Issues) originadas
 * de execuções de checklists, compartilhado entre os fluxos de submissão e edição.
 */
@Service
public class ChecklistIssueService {

    private static final int ISSUE_DUE_DAYS = 7;

    /**
     * Analisa as respostas fornecidas e cria novas pendências (Issues) para cada resposta marcada como 'NON_COMPLIANT'.
     * Evita a geração de pendências duplicadas para chaves de itens que já possuem issues associadas à execução.
     *
     * @param execution   a execução de checklist à qual as pendências serão atreladas.
     * @param answers     a lista de respostas enviadas na requisição.
     * @param itemsByKey  o mapa de itens do template para obter o título de cada item.
     */
    public void createIssuesForNonCompliantAnswers(
            ChecklistExecution execution,
            List<UpdateChecklistAnswerCommand> answers,
            Map<String, ChecklistItem> itemsByKey
    ) {
        Instant dueAt = Instant.now().plusSeconds(ISSUE_DUE_DAYS * 24L * 60L * 60L);

        Set<String> existingIssueKeys = execution.getIssues() != null
                ? execution.getIssues().stream().map((ChecklistIssue issue) -> issue.getItemKey()).collect(Collectors.toSet())
                : Set.of();

        answers.stream()
                .filter(answer -> answer.value() == ConformityAnswerValue.NON_COMPLIANT)
                .filter(answer -> !existingIssueKeys.contains(answer.itemKey()))
                .forEach(answer -> {
                    ChecklistItem item = itemsByKey.get(answer.itemKey());

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
     * Trunca uma String para garantir que não exceda o limite de caracteres das colunas do banco de dados.
     *
     * @param value     a String original a ser truncada.
     * @param maxLength o número máximo de caracteres permitido.
     * @return a String truncada ou o valor original se estiver dentro do limite de tamanho.
     */
    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
