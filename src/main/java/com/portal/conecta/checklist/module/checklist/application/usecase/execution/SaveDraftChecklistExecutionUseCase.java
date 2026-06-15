package com.portal.conecta.checklist.module.checklist.application.usecase.execution;
 
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecutionDraft;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.module.checklist.infrastructure.redis.ChecklistDraftRedisRepository;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistAnswerRequestDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionSaveDraftDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistAnswerResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistItemDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSchemaDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
 
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
 
/**
 * Caso de uso responsável pelo salvamento parcial (rascunho) de uma execução
 * de checklist <strong>exclusivamente no Redis</strong>.
 *
 * <p>O PostgreSQL não é tocado neste fluxo. A execução original persiste em Postgres
 * com {@code status = DRAFT} e {@code answers_json} vazio (como criada pelo
 * {@code CreateChecklistExecutionUseCase}). As respostas parciais vivem no Redis
 * até o submit final, quando o {@code SubmitChecklistExecutionUseCase} deve ler
 * o rascunho do Redis, validar e persistir no Postgres.</p>
 *
 * <h3>Regras de negócio aplicadas</h3>
 * <ol>
 *   <li>A execução deve existir no PostgreSQL (fonte de verdade para existência e metadados).</li>
 *   <li>O usuário autenticado deve ser o dono e ter permissão operacional sobre a turma.</li>
 *   <li>A execução deve estar com {@code status = DRAFT} no Postgres.</li>
 *   <li>Nenhuma resposta pode referenciar um {@code itemKey} inexistente no template.</li>
 *   <li>Respostas recebidas sobrescrevem as anteriores por {@code itemKey} no Redis;
 *       itens ausentes no payload são preservados.</li>
 *   <li>O TTL do rascunho no Redis é renovado a cada save (4h de inatividade → expira).</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
public class SaveDraftChecklistExecutionUseCase {
 
    private final ChecklistExecutionRepository executionRepository;
    private final ChecklistDraftRedisRepository draftRedisRepository;
    private final ChecklistExecutionMapper executionMapper;
    private final ObjectMapper objectMapper;
    private final RequestContextProvider contextProvider;
 
    /**
     * Salva respostas parciais do rascunho no Redis.
     *
     * @param executionId identificador único da execução no PostgreSQL.
     * @param request     DTO com as respostas a serem mescladas.
     * @return o {@link ChecklistExecutionDraft} persistido no Redis.
     * @throws EntityNotFoundException  se a execução não existir no Postgres.
     * @throws AccessDeniedException    se o usuário não tiver permissão.
     * @throws IllegalStateException    se a execução não estiver no status {@code DRAFT}.
     * @throws IllegalArgumentException se alguma resposta referenciar um item inválido no template.
     */
    public ChecklistExecutionDraft execute(UUID executionId, ChecklistExecutionSaveDraftDTO request) {
 
        // 1. Validar existência — Postgres é a fonte de verdade para metadados
        ChecklistExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execucao de checklist nao encontrada."));
 
        // 2. Validar permissões do usuário autenticado
        var currentUser = contextProvider.getRequestContext();
 
        if (!execution.getUserId().equals(currentUser.userId())
                || !currentUser.canSubmitChecklistExecutionForClass(execution.getClassId())) {
            throw new AccessDeniedException(
                    "Usuario nao tem permissao para editar esta execucao de checklist.");
        }
 
        // 3. Validar se a execução ainda aceita edição
        //    SUBMITTED e CANCELED são terminais — não podem ser reabertas pelo rascunho
        if (execution.getStatus() != ChecklistExecutionStatus.DRAFT) {
            throw new IllegalStateException(
                    "Somente execucoes em rascunho podem ser salvas parcialmente. " +
                    "Status atual: " + execution.getStatus());
        }
 
        // 4. Validar itemKeys contra o schema do template (apenas se houver respostas)
        if (!request.answers().isEmpty()) {
            Map<String, ChecklistItemDTO> itemsByKey = extractItemsByKey(execution);
            validateItemKeysExistInTemplate(itemsByKey, request.answers());
        }
 
        // 5. Carregar rascunho existente do Redis (ou iniciar um novo se ainda não existe)
        ChecklistExecutionDraft draft = draftRedisRepository
                .findByExecutionId(executionId)
                .orElseGet(() -> ChecklistExecutionDraft.builder()
                        .executionId(executionId)
                        .userId(execution.getUserId())
                        .classId(execution.getClassId())
                        .answers(new ArrayList<>())
                        .build());
 
        // 6. Mesclar as novas respostas com as já existentes no rascunho Redis
        List<ChecklistAnswerResponseDTO> merged = mergeAnswers(draft.getAnswers(), request.answers());
        draft.setAnswers(merged);
        draft.setLastModifiedAt(Instant.now());
 
        // 7. Persistir no Redis — renova o TTL de 4h automaticamente
        draftRedisRepository.save(draft);
 
        return draft;
    }
 
    // -------------------------------------------------------------------------
    // Métodos privados de suporte
    // -------------------------------------------------------------------------
 
    /**
     * Extrai e indexa os itens do schema do template vinculado à execução.
     *
     * @param execution execução cujo template será lido.
     * @return mapa {@code itemKey → ChecklistItemDTO}.
     */
    private Map<String, ChecklistItemDTO> extractItemsByKey(ChecklistExecution execution) {
        ChecklistSchemaDTO schema = objectMapper.convertValue(
                execution.getChecklistTemplate().getSchemaJson(),
                ChecklistSchemaDTO.class
        );
 
        return schema.sections().stream()
                .flatMap(section -> section.items().stream())
                .collect(Collectors.toMap(
                        ChecklistItemDTO::key,
                        Function.identity(),
                        (first, dup) -> {
                            throw new IllegalArgumentException(
                                    "item.key duplicado no template: " + first.key());
                        },
                        LinkedHashMap::new
                ));
    }
 
    /**
     * Garante que todas as respostas recebidas referenciam itens existentes no template.
     * Rascunho não valida obrigatoriedade — apenas rejeita itemKeys inválidas.
     *
     * @param itemsByKey      mapa de itens válidos do template.
     * @param incomingAnswers respostas enviadas pelo cliente.
     * @throws IllegalArgumentException se qualquer {@code itemKey} for inválida.
     */
    private void validateItemKeysExistInTemplate(
            Map<String, ChecklistItemDTO> itemsByKey,
            List<ChecklistAnswerRequestDTO> incomingAnswers
    ) {
        for (ChecklistAnswerRequestDTO answer : incomingAnswers) {
            if (!itemsByKey.containsKey(answer.itemKey())) {
                throw new IllegalArgumentException(
                        "Resposta enviada para item inexistente no template: " + answer.itemKey());
            }
        }
    }
 
    /**
     * Mescla as respostas já salvas no Redis com as novas recebidas no payload.
     *
     * <p>Estratégia:</p>
     * <ul>
     *   <li>Indexa as respostas existentes por {@code itemKey} em um {@code LinkedHashMap}
     *       (preserva ordem de inserção).</li>
     *   <li>Converte e insere as novas respostas — {@code put} sobrescreve se a chave já existe.</li>
     *   <li>Retorna a lista resultante sem duplicatas.</li>
     * </ul>
     *
     * @param existing        respostas já persistidas no Redis.
     * @param incomingAnswers novas respostas a serem mescladas.
     * @return lista mesclada e deduplicada por {@code itemKey}.
     */
    private List<ChecklistAnswerResponseDTO> mergeAnswers(
            List<ChecklistAnswerResponseDTO> existing,
            List<ChecklistAnswerRequestDTO> incomingAnswers
    ) {
        // Indexa existentes — O(n) inserção, O(1) lookup
        Map<String, ChecklistAnswerResponseDTO> mergedMap = existing.stream()
                .collect(Collectors.toMap(
                        ChecklistAnswerResponseDTO::itemKey,
                        Function.identity(),
                        (first, dup) -> dup,       // em inconsistência, a mais recente vence
                        LinkedHashMap::new
                ));
 
        // Converte e sobrescreve com as novas respostas
        for (ChecklistAnswerRequestDTO incoming : incomingAnswers) {
            mergedMap.put(incoming.itemKey(), executionMapper.toAnswerResponse(incoming));
        }
 
        return new ArrayList<>(mergedMap.values());
    }
}
 