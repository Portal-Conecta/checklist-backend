package com.portal.conecta.checklist.module.checklist.application.usecase.template;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistTemplateRepository;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistItemDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSchemaDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.update.ChecklistTemplateEditRequest;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.hub.provider.room.HubRoomProvider;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Caso de uso responsável pela edição de templates de checklist.
 *
 * <p>Apenas usuários com permissão de gerenciamento de templates podem realizar
 * alterações. Além disso, somente templates com status {@code DRAFT}
 * podem ser editados.</p>
 *
 * <p>A atualização é parcial: campos não informados na requisição
 * mantêm seus valores atuais.</p>
 */
@Service
@RequiredArgsConstructor
public class EditChecklistTemplateUseCase {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>(){};

    private final ChecklistTemplateRepository templateRepository;
    private final HubRoomProvider hubRoomProvider;
    private final RequestContextProvider contextProvider;
    private final ObjectMapper objectMapper;

    /**
     * Atualiza um template de checklist existente.
     *
     * <p>Antes da atualização são realizadas as seguintes validações:</p>
     * <ul>
     *     <li>Permissão do usuário autenticado;</li>
     *     <li>Existência do template informado;</li>
     *     <li>Status do template, que deve ser {@code DRAFT};</li>
     *     <li>Existência da sala informada no Hub;</li>
     *     <li>Unicidade das chaves de seções e itens do schema.</li>
     * </ul>
     *
     * @param templateId identificador do template a ser atualizado
     * @param request dados da atualização
     * @return template atualizado e persistido
     * @throws AccessDeniedException quando o usuário não possui permissão para editar templates
     * @throws EntityNotFoundException quando o template ou a sala não forem encontrados
     * @throws IllegalStateException quando o template não estiver com status {@code DRAFT}
     * @throws IllegalArgumentException quando houver chaves duplicadas no schema
     */
    @Transactional
    public ChecklistTemplate execute(UUID templateId, ChecklistTemplateEditRequest request){
        var currentUser = contextProvider.getRequestContext();

        if (!currentUser.canManageChecklistTemplates()){
            throw new AccessDeniedException("Usuário não tem permissão para editar templates de checklist.");
        }

        ChecklistTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("Template de checklist não encontrado."));

        if (template.getStatus() != ChecklistTemplateStatus.DRAFT){
            throw new IllegalStateException("Apenas templates com status DRAFT podem ser editados. Status atual: "  + template.getStatus());
        }

        if (request.roomId() != null){
            if (!hubRoomProvider.existsById(request.roomId())){
                throw new EntityNotFoundException("Sala não encontrada no Hub.");
            }
            template.setRoomId(request.roomId());
        }

        if (request.title() != null){
            template.setTitle(request.title());
        }

        if (request.description() != null){
            template.setDescription((request.description()));
        }

        if (request.schemaJson() != null){
            validateStableKeys(request.schemaJson());
            template.setSchemaJson(objectMapper.convertValue(request.schemaJson(), MAP_TYPE));
        }

        return  templateRepository.save(template);
    }

    /**
     * Valida a unicidade das chaves estáveis do schema.
     *
     * <p>Não é permitido que duas seções possuam a mesma {@code key}
     * nem que dois itens possuam a mesma {@code key}, independentemente
     * da seção em que estejam.</p>
     *
     * @param schema schema do checklist a ser validado
     * @throws IllegalArgumentException quando uma chave duplicada for encontrada
     */
    private void validateStableKeys(ChecklistSchemaDTO schema) {
        Set<String> sectionKeys = new HashSet<>();
        Set<String> itemKeys = new HashSet<>();

        schema.sections().forEach(section -> {
            if (!sectionKeys.add(section.key())) {
                throw new IllegalArgumentException("section.key duplicado: " + section.key());
            }
            section.items().stream()
                    .map(ChecklistItemDTO::key)
                    .forEach(itemKey -> {
                        if (!itemKeys.add(itemKey)) {
                            throw new IllegalArgumentException("item.key duplicado: " + itemKey);
                        }
                    });
        });
    }
}
