package com.portal.conecta.checklist.module.checklist.application.service.execution;

import com.portal.conecta.checklist.module.checklist.application.port.out.issue.CreateNonComplianceIssueCommand;
import com.portal.conecta.checklist.module.checklist.application.port.out.issue.IssueCreationPort;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.update.UpdateChecklistAnswerCommand;
import com.portal.conecta.checklist.module.checklist.domain.enums.ConformityAnswerValue;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.domain.schema.ChecklistItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Serviço responsável pelo gerenciamento de não-conformidades (Issues) originadas
 * de execuções de checklists, compartilhado entre os fluxos de submissão e edição.
 *
 * <p>Delega a criação de fato para {@link IssueCreationPort} (modulo Issues) —
 * ver ADR-0020. Este servico so orquestra: identifica quais respostas nao
 * conformes ainda nao tem pendencia associada.</p>
 */
@Service
@RequiredArgsConstructor
public class ChecklistIssueService {

    private final IssueCreationPort issueCreationPort;

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
        Set<String> existingIssueKeys = issueCreationPort.existingItemKeysForExecution(execution.getId());

        answers.stream()
                .filter(answer -> answer.value() == ConformityAnswerValue.NON_COMPLIANT)
                .filter(answer -> !existingIssueKeys.contains(answer.itemKey()))
                .forEach(answer -> {
                    ChecklistItem item = itemsByKey.get(answer.itemKey());

                    if (item != null) {
                        issueCreationPort.createNonComplianceIssue(new CreateNonComplianceIssueCommand(
                                execution.getId(),
                                execution.getUserId(),
                                answer.itemKey(),
                                item.title(),
                                answer.observation()
                        ));
                    }
                });
    }
}
