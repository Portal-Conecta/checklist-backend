package com.portal.conecta.checklist.module.checklist.application.usecase.execution;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.module.checklist.infrastructure.redis.ChecklistDraftRedisRepository;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistExecutionResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso responsavel por buscar e detalhar uma execucao de checklist pelo ID.
 *
 * <p>Realiza o merge transparente das respostas armazenadas no Redis caso o
 * checklist ainda se encontre em estado de rascunho (DRAFT), sem mutar a entidade
 * gerenciada pelo JPA.</p>
 */
@Service
@RequiredArgsConstructor
public class FindChecklistExecutionByIdUseCase {

    private final ChecklistExecutionRepository executionRepository;
    private final ChecklistDraftRedisRepository draftRedisRepository;
    private final ChecklistExecutionMapper executionMapper;
    private final RequestContextProvider contextProvider;

    /**
     * Recupera os detalhes de uma execucao de checklist, aplicando mesclagem com
     * o cache do Redis se aplicavel.
     *
     * @param executionId o identificador unico da execucao do checklist.
     * @return o DTO de resposta contendo todos os dados consolidados da execucao.
     */
    @Transactional(readOnly = true)
    public ChecklistExecutionResponseDTO execute(UUID executionId) {
        ChecklistExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execucao de checklist nao encontrada."));

        RequestContext currentUser = contextProvider.getRequestContext();
        if (!execution.getUserId().equals(currentUser.userId())) {
            throw new AccessDeniedException("Usuario nao tem permissao para visualizar esta execucao de checklist.");
        }

        ChecklistExecutionResponseDTO responseDto = executionMapper.toResponse(execution);

        if (execution.getStatus() == ChecklistExecutionStatus.DRAFT) {
            return draftRedisRepository.findByExecutionId(executionId)
                    .map(draft -> executionMapper.toResponseWithAnswers(execution, draft.getAnswers()))
                    .orElseGet(() -> executionMapper.toResponse(execution));
            }

        return executionMapper.toResponse(execution);
    }
}