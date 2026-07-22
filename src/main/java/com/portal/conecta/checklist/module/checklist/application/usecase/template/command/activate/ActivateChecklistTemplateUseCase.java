package com.portal.conecta.checklist.module.checklist.application.usecase.template.command.activate;

import com.portal.conecta.checklist.module.checklist.application.port.out.integration.HubRoomProvider;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.application.port.out.persistence.ChecklistTemplateRepositoryPort;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso responsável por ativar uma versão de um template de checklist.
 *
 * <p>A ativação é uma operação administrativa e só pode ser realizada por
 * usuários com permissão para gerenciar templates.</p>
 *
 * <p>Ao ativar um template, qualquer outra versão do mesmo grupo que esteja
 * com status {@code ACTIVE} é automaticamente marcada como
 * {@code INACTIVE}, garantindo que exista apenas uma versão ativa por grupo.</p>
 */
@Service
@RequiredArgsConstructor
public class ActivateChecklistTemplateUseCase {

    private final ChecklistTemplateRepositoryPort templateRepository;
    private final RequestContextProvider contextProvider;
    private final HubRoomProvider hubRoomProvider;
    /**
     * Ativa um template de checklist.
     *
     * <p>Antes da ativação são realizadas as seguintes validações:</p>
     * <ul>
     *     <li>Permissão do usuário autenticado;</li>
     *     <li>Existência do template informado;</li>
     *     <li>Status do template, que deve ser {@code DRAFT}.</li>
     * </ul>
     *
     * <p>Após a validação, versões ativas anteriores pertencentes ao mesmo
     * grupo de templates são desativadas e o template informado passa a ser
     * a versão ativa.</p>
     *
     * @param templateId identificador do template a ser ativado
     * @return template ativado e persistido
     * @throws AccessDeniedException quando o usuário não possui permissão para ativar templates
     * @throws EntityNotFoundException quando o template não for encontrado
     * @throws IllegalStateException quando o template não estiver com status {@code DRAFT}
     */
    @Transactional
    public ChecklistTemplate execute(UUID templateId) {
        var currentUser = contextProvider.getRequestContext();

        if (!currentUser.canManageChecklistTemplates()) {
            throw new AccessDeniedException("Usuario nao tem permissao para ativar templates de checklist.");
        }

        ChecklistTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("Template de checklist nao encontrado."));

        if (template.getStatus() != ChecklistTemplateStatus.DRAFT) {
            throw new IllegalStateException("Apenas templates com status DRAFT podem ser ativados. Status atual: " + template.getStatus());
        }

        hubRoomProvider.findById(template.getRoomId())
                .orElseThrow(() -> new IllegalStateException("A sala vinculada a este template foi removida do Hub. Não é possível ativar o template."));

        inactivatePreviousVersion(template);

        template.setStatus(ChecklistTemplateStatus.ACTIVE);
        template.setActive(true);

        return templateRepository.save(template);
    }

    /**
     * Desativa versões ativas anteriores pertencentes ao mesmo grupo do template informado.
     *
     * <p>Essa validação garante que apenas uma versão de cada grupo permaneça
     * com status {@code ACTIVE}.</p>
     *
     * @param template template que será promovido para a versão ativa
     */
    private void inactivatePreviousVersion(ChecklistTemplate template) {
        templateRepository
                .findByTemplateGroupIdAndStatus(template.getTemplateGroupId(), ChecklistTemplateStatus.ACTIVE)
                .stream()
                .filter(active -> !Objects.equals(active.getId(), template.getId()))
                .forEach(active -> {
                    active.setStatus(ChecklistTemplateStatus.INACTIVE);
                    active.setActive(false);
                    // flush imediato: garante que a desativação chegue ao banco antes da
                    // ativação da nova versão (índice único parcial permite só 1 ACTIVE
                    // por grupo — sem isso, o Hibernate pode emitir as duas UPDATEs fora
                    // de ordem e violar a constraint mesmo o estado final sendo válido).
                    templateRepository.saveAndFlushTemplate(active);
                });
    }
}
