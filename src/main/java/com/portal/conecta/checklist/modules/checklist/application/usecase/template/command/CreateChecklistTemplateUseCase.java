package com.portal.conecta.checklist.modules.checklist.application.usecase.template.command;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistTemplateRepositoryPort;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.modules.checklist.application.port.out.integration.HubRoomProvider;
import com.portal.conecta.checklist.shared.utils.ChecklistSchemaValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

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

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ChecklistTemplateRepositoryPort templateRepository;
    private final HubRoomProvider hubRoomProvider;
    private final RequestContextProvider contextProvider;
    private final ObjectMapper objectMapper;

    @Transactional
    public ChecklistTemplate execute(CreateChecklistTemplateCommand command) {
        var currentUser = contextProvider.getRequestContext();

        if (!currentUser.canManageChecklistTemplates()) {
            throw new AccessDeniedException("Usuario nao tem permissao para criar templates de checklist.");
        }

        if (!hubRoomProvider.existsById(command.roomId())) {
            throw new EntityNotFoundException("Sala nao encontrada no Hub.");
        }

        ChecklistSchemaValidator.validateStableKeys(command.schema());

        var template = ChecklistTemplate.builder()
                .roomId(command.roomId())
                .title(command.title())
                .description(command.description() == null ? "" : command.description())
                .version(1)
                .status(ChecklistTemplateStatus.DRAFT)
                .active(false)
                .schemaJson(objectMapper.convertValue(command.schema(), MAP_TYPE))
                .templateGroupId(UUID.randomUUID())
                .build();

        return templateRepository.save(template);
    }


}
