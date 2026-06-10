package com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.submit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.core.ChecklistIssueService;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.enums.ConformityAnswerValue;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.UserReference;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistAnswerRequestDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionSubmitDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistItemDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSchemaDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import com.portal.conecta.checklist.module.issues.domain.enums.IssuePriority;
import com.portal.conecta.checklist.module.issues.domain.enums.IssueStatus;
import com.portal.conecta.checklist.module.issues.domain.model.ChecklistIssue;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Caso de uso responsavel por submeter uma execucao de checklist.
 *
 * <p>Valida permissao, status, respostas obrigatorias e regras de nao
 * conformidade antes de consolidar o checklist como enviado.</p>
 */
@Service
@RequiredArgsConstructor
public class SubmitChecklistExecutionUseCase {

    private static final int ISSUE_DUE_DAYS = 7;

    private final ChecklistExecutionRepository executionRepository;
    private final ChecklistExecutionMapper executionMapper;
    private final ObjectMapper objectMapper;
    private final RequestContextProvider contextProvider;
    private final ChecklistIssueService issueService;
    /**
     * Submete e processa a execução de um checklist.
     * <p>
     * O método realiza validações de permissão do usuário atual, verifica se a execução
     * está no status correto (rascunho), mapeia e valida as respostas enviadas contra o
     * schema original do template, calcula o score de conformidade e gera pendências
     * (issues) para itens não conformes.
     * </p>
     *
     * @param executionId o identificador único da execução do checklist.
     * @param request     o DTO contendo os dados de submissão, incluindo as respostas.
     * @return a entidade {@link ChecklistExecution} salva com o status atualizado para SUBMITTED.
     * @throws EntityNotFoundException  se a execução do checklist não for encontrada.
     * @throws AccessDeniedException    se o usuário logado não tiver permissão para enviar o checklist.
     * @throws IllegalStateException    se o checklist não estiver no status {@code DRAFT}.
     * @throws IllegalArgumentException se houver falhas na validação das respostas.
     */



    @Transactional
    public ChecklistExecution execute(UUID executionId, ChecklistExecutionSubmitDTO request) {
        ChecklistExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execucao de checklist nao encontrada."));

        var currentUser = contextProvider.getRequestContext();

        if (!execution.getUserId().equals(currentUser.userId())
                || !currentUser.canSubmitChecklistExecutionForClass(execution.getClassId())) {
            throw new AccessDeniedException("Usuario nao tem permissao para enviar esta execucao de checklist.");
        }

        if (execution.getStatus() != ChecklistExecutionStatus.DRAFT) {
            throw new IllegalStateException("Somente checklists em rascunho podem ser enviados.");
        }

        ChecklistSchemaDTO schema = objectMapper.convertValue(
                execution.getChecklistTemplate().getSchemaJson(),
                ChecklistSchemaDTO.class
        );

        Map<String, ChecklistItemDTO> itemsByKey = itemsByKey(schema);
        Map<String, ChecklistAnswerRequestDTO> answersByItemKey = answersByItemKey(request.answers());

        validateAnswers(itemsByKey, answersByItemKey);

        execution.setAnswersJson(executionMapper.toAnswersJson(request));
        execution.setComplianceScore(calculateComplianceScore(request.answers()));
        execution.setStatus(ChecklistExecutionStatus.SUBMITTED);
        execution.setSubmittedAt(LocalDateTime.now());
        createIssuesForNonCompliantAnswers(execution, request.answers(), itemsByKey);

        issueService.createIssuesForNonCompliantAnswers(execution, request.answers(), itemsByKey);

        return executionRepository.save(execution);
    }
    /**
     * Mapeia os itens do esquema do checklist utilizando suas chaves únicas.
     *
     * @param schema o esquema do checklist contendo seções e itens.
     * @return um mapa ordenado onde a chave é o identificador do item e o valor é o próprio item.
     * @throws IllegalArgumentException se houver chaves duplicadas no template.
     */
    private Map<String, ChecklistItemDTO> itemsByKey(ChecklistSchemaDTO schema) {
        return schema.sections().stream()
                .flatMap(section -> section.items().stream())
                .collect(Collectors.toMap(
                        ChecklistItemDTO::key,
                        Function.identity(),
                        (first, duplicated) -> {
                            throw new IllegalArgumentException("item.key duplicado no template: " + first.key());
                        },
                        LinkedHashMap::new
                ));
    }

    /**
     * Mapeia as respostas enviadas na requisição utilizando as chaves dos itens respondidos.
     *
     * @param answers a lista de respostas fornecidas pelo usuário.
     * @return um mapa ordenado onde a chave é o identificador do item (itemKey) e o valor é a resposta.
     * @throws IllegalArgumentException se houver mais de uma resposta para a mesma chave.
     */
    private Map<String, ChecklistAnswerRequestDTO> answersByItemKey(List<ChecklistAnswerRequestDTO> answers) {
        return answers.stream()
                .collect(Collectors.toMap(
                        ChecklistAnswerRequestDTO::itemKey,
                        Function.identity(),
                        (first, duplicated) -> {
                            throw new IllegalArgumentException("Resposta duplicada para itemKey: " + first.itemKey());
                        },
                        LinkedHashMap::new
                ));
    }

    /**
     * Valida as respostas fornecidas pelo usuário comparando-as com os itens do template.
     * <p>
     * As seguintes regras são aplicadas:
     * 1. Nenhuma resposta pode ser enviada para um item que não existe no template.
     * 2. Todos os itens marcados como obrigatórios no template devem ter uma resposta.
     * 3. Respostas marcadas como NÃO CONFORME (NON_COMPLIANT) exigem uma observação preenchida.
     * </p>
     *
     * @param itemsByKey       mapa de itens extraídos do template original.
     * @param answersByItemKey mapa de respostas enviadas na requisição.
     * @throws IllegalArgumentException se alguma das regras de validação for violada.
     */

    private void validateAnswers(
            Map<String, ChecklistItemDTO> itemsByKey,
            Map<String, ChecklistAnswerRequestDTO> answersByItemKey
    ) {
        for (String answerItemKey : answersByItemKey.keySet()) {
            if (!itemsByKey.containsKey(answerItemKey)) {
                throw new IllegalArgumentException("Resposta enviada para item inexistente no template: " + answerItemKey);
            }
        }

        for (ChecklistItemDTO item : itemsByKey.values()) {
            ChecklistAnswerRequestDTO answer = answersByItemKey.get(item.key());

            if (Boolean.TRUE.equals(item.required()) && answer == null) {
                throw new IllegalArgumentException("Item obrigatorio sem resposta: " + item.key());
            }
        }

        answersByItemKey.values().forEach(answer -> {
            if (answer.value() == ConformityAnswerValue.NON_COMPLIANT && isBlank(answer.observation())) {
                throw new IllegalArgumentException("Item nao conforme exige observacao: " + answer.itemKey());
            }
        });
    }
    /**
     * Calcula a nota de conformidade (Compliance Score) com base nas respostas avaliadas.
     * O cálculo é feito dividindo o número de itens conformes pelo total de itens respondidos.
     *
     * @param answers a lista de respostas fornecidas pelo usuário.
     * @return o percentual de conformidade de 0.00 a 100.00, com duas casas decimais.
     */
    private BigDecimal calculateComplianceScore(List<ChecklistAnswerRequestDTO> answers) {
        long answeredItems = answers.stream()
                .filter(answer -> answer.value() != null)
                .count();

        if (answeredItems == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        long compliantItems = answers.stream()
                .filter(answer -> answer.value() == ConformityAnswerValue.COMPLIANT)
                .count();

        return BigDecimal.valueOf(compliantItems)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(answeredItems), 2, RoundingMode.HALF_UP);
    }
    /**
     * Verifica se uma String é nula, vazia ou contém apenas espaços em branco.
     *
     * @param value a String a ser verificada.
     * @return {@code true} se for nula ou em branco, {@code false} caso contrário.
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }




    /**
     * Trunca uma String para garantir que não exceda um tamanho máximo estipulado.
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
