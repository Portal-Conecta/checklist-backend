package com.portal.conecta.checklist.module.checklist.presentation.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.enums.ConformityAnswerValue;
import com.portal.conecta.checklist.module.checklist.domain.enums.Period;
import com.portal.conecta.checklist.module.checklist.domain.enums.Shift;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistAnswerRequestDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionDraftCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionSubmitDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.*;
import com.portal.conecta.checklist.module.issues.presentation.mapper.ChecklistIssueMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Mapper responsável por converter execuções de checklist entre entidades de domínio,
 * estruturas JSON de respostas e DTOs usados pela camada de apresentação.
 *
 * <p>Converte DTOs e entidades, alem de transformar respostas entre objetos
 * tipados e a representacao JSON persistida.</p>
 */
@Component
public class ChecklistExecutionMapper {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final ChecklistIssueMapper issueMapper;

    public ChecklistExecutionMapper(ObjectMapper objectMapper, ChecklistIssueMapper issueMapper) {
        this.objectMapper = objectMapper;
        this.issueMapper = issueMapper;
    }

    /**
     * Cria uma entidade de execução em rascunho a partir da requisição inicial.
     *
     * @param request dados informados para criar o rascunho.
     * @param template template de checklist associado à execução.
     * @param filledBy identificador do usuário responsável pelo preenchimento.
     * @param startedAt data e hora de início da execução.
     * @return entidade de execução em rascunho ou {@code null} quando a requisição for nula.
     */
    public ChecklistExecution toDraftEntity(
            ChecklistExecutionDraftCreateDTO request,
            ChecklistTemplate template,
            UUID filledBy,
            LocalDateTime startedAt,
            Shift shift,
            Period period
    ) {
        if (request == null) {
            return null;
        }

        return ChecklistExecution.builder()
                .checklistTemplate(template)
                .roomId(request.roomId())
                .classId(request.classId())
                .userId(filledBy)
                .shift(shift)
                .period(period)
                .checklistType(request.checklistType())
                .status(ChecklistExecutionStatus.DRAFT)
                .answersJson(toAnswersJson(emptyAnswers()))
                .startedAt(startedAt)
                .build();
    }

    /**
     * Converte uma execução de checklist para o DTO completo de resposta.
     *
     * @param execution entidade de execução a ser convertida.
     * @return DTO completo da execução ou {@code null} quando a entidade for nula.
     */
    public ChecklistExecutionResponseDTO toResponse(ChecklistExecution execution) {
        if (execution == null) {
            return null;
        }

        ChecklistTemplate template = execution.getChecklistTemplate();
        ChecklistAnswersDTO answers = toAnswersDTO(execution.getAnswersJson());

        return new ChecklistExecutionResponseDTO(
                execution.getId(),
                template == null ? null : template.getId(),
                template == null ? null : template.getVersion(),
                execution.getRoomId(),
                execution.getClassId(),
                execution.getUserId(),
                execution.getPeriod(),
                execution.getChecklistType(),
                execution.getStatus(),
                execution.getComplianceScore(),
                answers,
                answers.summary(),
                toInstant(execution.getStartedAt()),
                toInstant(execution.getSubmittedAt()),
                issueMapper.toResponseList(execution.getIssues())
        );
    }

    /**
     * Converte a requisição de submissão para o JSON estruturado de respostas.
     *
     * @param request requisição contendo as respostas enviadas.
     * @return mapa compatível com o campo JSON de respostas da entidade.
     */
    public Map<String, Object> toAnswersJson(ChecklistExecutionSubmitDTO request) {
        if (request == null) {
            return toAnswersJson(emptyAnswers());
        }

        List<ChecklistAnswerResponseDTO> answers = safeAnswers(request.answers()).stream()
                .map(this::toAnswerResponse)
                .filter(Objects::nonNull)
                .toList();

        return toAnswersJson(new ChecklistAnswersDTO(answers, summarize(answers)));
    }

    /**
     * Converte um DTO de respostas para a estrutura JSON persistível.
     *
     * @param answers DTO de respostas e resumo.
     * @return mapa compatível com o campo JSON de respostas da entidade.
     */
    public Map<String, Object> toAnswersJson(ChecklistAnswersDTO answers) {
        return objectMapper.convertValue(answers == null ? emptyAnswers() : answers, MAP_TYPE);
    }

    /**
     * Converte a estrutura JSON persistida para DTO de respostas.
     *
     * @param answersJson mapa persistido com itens e resumo das respostas.
     * @return DTO de respostas ou uma estrutura vazia quando o JSON for nulo ou vazio.
     */
    public ChecklistAnswersDTO toAnswersDTO(Map<String, Object> answersJson) {
        if (answersJson == null || answersJson.isEmpty()) {
            return emptyAnswers();
        }

        return objectMapper.convertValue(answersJson, ChecklistAnswersDTO.class);
    }

    /**
     * Converte uma resposta recebida na submissão para o DTO usado nas respostas persistidas.
     *
     * @param answer resposta informada para um item do checklist.
     * @return resposta convertida ou {@code null} quando a entrada for nula.
     */
    public ChecklistAnswerResponseDTO toAnswerResponse(ChecklistAnswerRequestDTO answer) {
        if (answer == null) {
            return null;
        }

        Boolean compliant = answer.value() == null ? null : answer.value() == ConformityAnswerValue.COMPLIANT;

        return new ChecklistAnswerResponseDTO(
                answer.itemKey(),
                answer.value(),
                compliant,
                answer.observation(),
                answer.answeredAt()
        );
    }

    /**
     * Converte uma execução para o DTO resumido usado na consulta de histórico.
     *
     * @param execution execução de checklist a ser convertida.
     * @return item de histórico ou {@code null} quando a execução for nula.
     */
    public ChecklistExecutionHistoryDTO toHistoryResponse(ChecklistExecution execution) {
        if (execution == null) {
            return null;
        }

        ChecklistTemplate template = execution.getChecklistTemplate();
        ChecklistAnswersDTO answers = toAnswersDTO(execution.getAnswersJson());

        return new ChecklistExecutionHistoryDTO(
                execution.getId(),
                template == null ? null : template.getId(),
                template == null ? null : template.getVersion(),
                execution.getRoomId(),
                execution.getClassId(),
                execution.getUserId(),
                execution.getPeriod(),
                execution.getChecklistType(),
                execution.getStatus(),
                execution.getComplianceScore(),
                toInstant(execution.getStartedAt()),
                toInstant(execution.getSubmittedAt()),
                answers == null ? null : answers.summary()
        );
    }

    public Page<ChecklistExecutionHistoryDTO> toPageHistory(Page<ChecklistExecution> checklistExecutions) {
        if (checklistExecutions == null) {
            return Page.empty();
        }

        return checklistExecutions.map(this::toHistoryResponse);
    }

    /**
     * Converte uma lista de execuções para itens de histórico.
     *
     * @param executions execuções retornadas pela consulta.
     * @return lista de DTOs de histórico ou lista vazia quando a entrada for nula.
     */
    public List<ChecklistExecutionHistoryDTO> toHistoryResponseList(List<ChecklistExecution> executions) {
        if (executions == null) {
            return List.of();
        }

        return executions.stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private ChecklistAnswersDTO emptyAnswers() {
        return new ChecklistAnswersDTO(List.of(), new ChecklistExecutionSummaryDTO(0, 0, 0, 0));
    }

    private ChecklistExecutionSummaryDTO summarize(List<ChecklistAnswerResponseDTO> answers) {
        int totalItems = answers.size();
        int answeredItems = (int) answers.stream()
                .filter(answer -> answer.value() != null)
                .count();
        int compliantItems = (int) answers.stream()
                .filter(answer -> Boolean.TRUE.equals(answer.compliant()))
                .count();
        int nonCompliantItems = (int) answers.stream()
                .filter(answer -> Boolean.FALSE.equals(answer.compliant()))
                .count();

        return new ChecklistExecutionSummaryDTO(totalItems, answeredItems, compliantItems, nonCompliantItems);
    }

    private List<ChecklistAnswerRequestDTO> safeAnswers(List<ChecklistAnswerRequestDTO> answers) {
        return answers == null ? List.of() : answers;
    }

    private Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }
}
