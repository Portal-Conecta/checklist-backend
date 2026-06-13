package com.portal.conecta.checklist.modules.checklist.application.usecase.template.command;

import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistTemplateRepositoryPort;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.hub.provider.room.HubRoomProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso responsável pela criação de uma nova versão de um template de checklist.
 *
 * <p>A nova versão é criada a partir de um template existente com status
 * {@code ACTIVE}, copiando suas informações e incrementando o número da versão.
 * O template gerado inicia com status {@code DRAFT} e permanece inativo até sua publicação.</p>
 */
@Service
@RequiredArgsConstructor
public class CreateChecklistTemplateVersionUseCase {

    private final ChecklistTemplateRepositoryPort templateRepository;
    private final RequestContextProvider contextProvider;
    private final HubRoomProvider hubRoomProvider;
    /**
     * Cria uma nova versão de um template de checklist.
     *
     * <p>Antes da criação são realizadas as seguintes validações:</p>
     * <ul>
     *     <li>Permissão do usuário autenticado para gerenciar templates;</li>
     *     <li>Existência do template informado;</li>
     *     <li>Status do template, que deve ser {@code ACTIVE}.</li>
     * </ul>
     *
     * <p>A nova versão herda os dados do template original, recebe o número
     * da versão incrementado em uma unidade e é criada com status
     * {@code DRAFT}.</p>
     *
     * @param templateId identificador do template que servirá como base para a nova versão
     * @return nova versão do template persistida
     * @throws AccessDeniedException quando o usuário não possui permissão para versionar templates
     * @throws EntityNotFoundException quando o template não for encontrado
     * @throws IllegalStateException quando o template não estiver com status {@code ACTIVE}
     */
    @Transactional
    public ChecklistTemplate execute(UUID templateId) {
        var currentUser = contextProvider.getRequestContext();

        if (!currentUser.canManageChecklistTemplates()) {
            throw new AccessDeniedException("Usuário não tem permissão para versionar templates de checklist.");
        }

        ChecklistTemplate origin = templateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("Template de checklist não encontrado."));

        if (origin.getStatus() != ChecklistTemplateStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Apenas templates Active podem gerar uma nova versão. Status atual: "
                            + origin.getStatus()
            );
        }

        hubRoomProvider.findById(origin.getRoomId())
                .orElseThrow(() -> new IllegalStateException("A sala vinculada a este template foi removida do Hub. Não é possível criar uma nova versão."));


        ChecklistTemplate newVersion = ChecklistTemplate.builder()
                .templateGroupId(origin.getTemplateGroupId())
                .roomId(origin.getRoomId())
                .title(origin.getTitle())
                .description(origin.getDescription())
                .schemaJson(origin.getSchemaJson())
                .version(origin.getVersion() + 1)
                .status(ChecklistTemplateStatus.DRAFT)
                .active(false)
                .build();

        return templateRepository.save(newVersion);
    }
}