package com.portal.conecta.checklist.module.checklist.application.usecase.template;

import com.portal.conecta.checklist.modules.checklist.application.port.out.integration.HubRoomProvider;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.modules.checklist.infrastructure.persistence.ChecklistTemplateRepository;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.template.request.ChecklistTemplateCreateRequest;
import com.portal.conecta.checklist.modules.checklist.presentation.mapper.ChecklistTemplateMapper;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.utils.ChecklistSchemaValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Caso de uso responsavel pela criacao de templates de checklist.
 *
 * <p>Aplica autorizacao gerencial, valida a existencia da sala no Hub e
 * garante que chaves de secoes e itens sejam estaveis e unicas dentro do
 * schema recebido.</p>
 */
@Service
@RequiredArgsConstructor
public class CreateChecklistTemplateUseCase {

    private final ChecklistTemplateRepository templateRepository;
    private final HubRoomProvider hubRoomProvider;
    private final RequestContextProvider contextProvider;
    private final ChecklistTemplateMapper templateMapper;

    @Transactional
    public ChecklistTemplate execute(ChecklistTemplateCreateRequest request) {
        var currentUser = contextProvider.getRequestContext();

        if (!currentUser.canManageChecklistTemplates()) {
            throw new AccessDeniedException("Usuario nao tem permissao para criar templates de checklist.");
        }

        var room = hubRoomProvider.findById(request.roomId())
                .orElseThrow(() -> new EntityNotFoundException("Sala nao encontrada no Hub."));

        ChecklistSchemaValidator.validateStableKeys(request.schemaJson());

        var template = templateMapper.toEntity(request);
        return templateRepository.save(template);
    }


}
