package com.portal.conecta.checklist.modules.checklist.application.usecase.template.command;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistTemplateRepositoryPort;
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

    private final ChecklistTemplateRepositoryPort templateRepository;
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
    public ChecklistTemplate execute(UUID templateId, EditChecklistTemplateCommand command){
        var currentUser = contextProvider.getRequestContext();

        if (!currentUser.canManageChecklistTemplates()){
            throw new AccessDeniedException("Usuário não tem permissão para editar templates de checklist.");
        }

        ChecklistTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("Template de checklist não encontrado."));

        if (template.getStatus() != ChecklistTemplateStatus.DRAFT){
            throw new IllegalStateException("Apenas templates com status DRAFT podem ser editados. Status atual: "  + template.getStatus());
        }

        if (command.title() != null){
            template.setTitle(command.title());
        }

        if (command.description() != null){
            template.setDescription(command.description());
        }

        if (command.schema() != null){
            ChecklistSchemaValidator.validateStableKeys(command.schema());
            template.setSchemaJson(objectMapper.convertValue(command.schema(), MAP_TYPE));
        }

        return  templateRepository.save(template);
    }



}
