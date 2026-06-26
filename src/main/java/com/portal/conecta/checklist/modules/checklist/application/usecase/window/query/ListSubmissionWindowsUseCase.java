package com.portal.conecta.checklist.modules.checklist.application.usecase.window.query;

import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistSubmissionWindow;
import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistSubmissionWindowRepositoryPort;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso responsavel por consultar janelas de envio de checklist.
 *
 * <p>A listagem global e restrita a perfis gerenciais (SENAI/WEG), por expor a
 * configuracao de todas as turmas. A consulta por turma permanece disponivel
 * para os perfis com acesso ao modulo, conforme necessidade do frontend.</p>
 */
@Service
@RequiredArgsConstructor
public class ListSubmissionWindowsUseCase {

    private final ChecklistSubmissionWindowRepositoryPort repository;
    private final RequestContextProvider contextProvider;

    @Transactional(readOnly = true)
    public List<ChecklistSubmissionWindow> execute() {
        RequestContext currentUser = contextProvider.getRequestContext();

        if (!currentUser.canManageChecklistTemplates()) {
            throw new AccessDeniedException(
                    "Usuario nao tem permissao para listar as janelas de envio."
            );
        }

        return repository.findAllByOrderByClassIdAscChecklistTypeAsc();
    }

    @Transactional(readOnly = true)
    public List<ChecklistSubmissionWindow> execute(UUID classId) {
        return repository.findAllByClassIdOrderByChecklistTypeAsc(classId);
    }
}
