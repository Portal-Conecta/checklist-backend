package com.portal.conecta.checklist.module.checklist.application.usecase.execution;


import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistTemplateRepository;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionDraftCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.hub.provider.classes.HubClassProvider;
import com.portal.conecta.checklist.shared.hub.provider.room.HubRoomProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Caso de uso responsavel por criar uma execucao de checklist em rascunho.
 *
 * <p>Valida template, sala, turma, permissao do usuario autenticado e
 * duplicidade por turma, sala, periodo, dia e tipo antes de persistir a
 * execucao.</p>
 */
@Service
@RequiredArgsConstructor
public class CreateChecklistExecutionUseCase {

    private final ChecklistExecutionRepository repository;
    private final ChecklistTemplateRepository templateRepository;
    private final ChecklistExecutionMapper executionMapper;
    private final RequestContextProvider contextProvider;
    private final HubRoomProvider hubRoomProvider;
    private final HubClassProvider hubClassProvider;

    /**
     * Caso de uso responsável pela criação de uma nova execução de checklist (em status de rascunho).
     * <p>
     * Valida os dados do template, a existência da sala e turma nos provedores externos (Hub),
     * as permissões do usuário logado e se já existe um checklist idêntico criado no mesmo dia.
     * </p>
     */

    @Transactional
    public ChecklistExecution execute(ChecklistExecutionDraftCreateDTO request) {
        ChecklistTemplate template = templateRepository.findById(request.templateId())
                .orElseThrow(() -> new EntityNotFoundException("Template nao encontrado."));

        /**
         * Inicia uma nova execução de checklist a partir de um template existente.
         * <p>
         * O processo envolve as seguintes validações:
         * 1. O template deve existir, estar ativo e estar associado à mesma sala informada na requisição.
         * 2. A sala e a turma informadas devem existir nos registros do Hub.
         * 3. O usuário logado deve ter as permissões necessárias para operar na turma especificada.
         * 4. Não pode existir um checklist já criado para a mesma turma, sala, período e tipo na data de hoje.
         * </p>
         * Se todas as validações passarem, uma nova entidade {@link ChecklistExecution} no status rascunho (DRAFT) será salva.
         *
         * @param request os dados iniciais necessários para a criação do rascunho do checklist.
         * @return a entidade {@link ChecklistExecution} recém-criada e persistida.
         * @throws EntityNotFoundException  se o template, a sala ou a turma não forem encontrados.
         * @throws IllegalStateException    se o template selecionado não estiver ativo.
         * @throws IllegalArgumentException se o template não pertencer à sala informada ou se já houver um checklist duplicado no mesmo dia.
         * @throws AccessDeniedException    se o usuário atual não tiver permissão para criar o checklist para a turma.
         */


        if (!template.isActive() || template.getStatus() != ChecklistTemplateStatus.ACTIVE) {
            throw new IllegalStateException("Template nao esta ativo.");
        }

        if (!template.getRoomId().equals(request.roomId())) {
            throw new IllegalArgumentException("Template nao pertence a sala informada.");
        }

        if (!hubRoomProvider.existsById(request.roomId())) {
            throw new EntityNotFoundException("Sala nao encontrada no Hub.");
        }

        if (!hubClassProvider.existsById(request.classId())) {
            throw new EntityNotFoundException("Turma nao encontrada no Hub.");
        }

        RequestContext currentUser = contextProvider.getRequestContext();

        if (!currentUser.canOperateChecklistExecutionForClass(request.classId())) {
            throw new AccessDeniedException("Usuario nao tem permissao para criar checklist para a turma informada.");
        }

        var now = LocalDateTime.now();
        var startOfDay = now.toLocalDate().atStartOfDay();
        var endOfDay = startOfDay.plusDays(1);

        boolean duplicated = repository.existsDuplicateChecklist(
                request.classId(),
                request.roomId(),
                request.period().name(),
                request.checklistType().name(),
                startOfDay,
                endOfDay
        );

        if (duplicated) {
            throw new IllegalArgumentException("Ja existe checklist para esta turma, sala, periodo, dia e tipo.");
        }

        ChecklistExecution execution = executionMapper.toDraftEntity(request, template, currentUser.userId(), now);

        return repository.save(execution);
    }
}
