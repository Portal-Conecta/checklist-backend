package com.portal.conecta.checklist.modules.checklist.application.usecase.template.command.edit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.modules.checklist.application.port.out.integration.HubRoomProvider;
import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistTemplateRepositoryPort;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.utils.ChecklistSchemaValidator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Caso de uso responsavel pela edicao parcial de templates de checklist.
 *
 * <p>Apenas usuarios com permissao de gerenciamento podem editar templates, e
 * somente templates em status {@code DRAFT} aceitam alteracoes.</p>
 */
@Service
@RequiredArgsConstructor
public class EditChecklistTemplateUseCase {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ChecklistTemplateRepositoryPort templateRepository;
    private final RequestContextProvider contextProvider;
    private final ObjectMapper objectMapper;
    private final HubRoomProvider hubRoomProvider;

    @Transactional
    public ChecklistTemplate execute(UUID templateId, EditChecklistTemplateCommand command) {
        var currentUser = contextProvider.getRequestContext();

        if (!currentUser.canManageChecklistTemplates()) {
            throw new AccessDeniedException("Usuario nao tem permissao para editar templates de checklist.");
        }

        ChecklistTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("Template de checklist nao encontrado."));

        if (template.getStatus() != ChecklistTemplateStatus.DRAFT) {
            throw new IllegalStateException("Apenas templates com status DRAFT podem ser editados. Status atual: " + template.getStatus());
        }

        hubRoomProvider.findById(template.getRoomId())
                .orElseThrow(() -> new IllegalStateException("A sala vinculada a este template foi removida do Hub. A edicao nao e mais permitida."));

        if (command.title() != null) {
            template.setTitle(command.title());
        }

        if (command.description() != null) {
            template.setDescription(command.description());
        }

        if (command.schema() != null) {
            ChecklistSchemaValidator.validateStableKeys(command.schema());
            template.setSchemaJson(objectMapper.convertValue(command.schema(), MAP_TYPE));
        }

        return templateRepository.save(template);
    }
}
