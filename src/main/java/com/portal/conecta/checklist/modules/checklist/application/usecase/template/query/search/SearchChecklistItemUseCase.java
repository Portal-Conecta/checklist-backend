package com.portal.conecta.checklist.modules.checklist.application.usecase.template.query.search;

import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistItemSearchPort;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistItem;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchChecklistItemUseCase {

    private final ChecklistItemSearchPort itemSearchPort;
    private final RequestContextProvider requestContextProvider;

    public List<ChecklistItem> execute(String searchTerm) {
        var context = requestContextProvider.getRequestContext();

        if (!context.canAccessChecklistModule()) {
            throw new IllegalArgumentException("Usuário não autorizado a acessar o módulo de checklist.");
        }

        if (searchTerm == null || searchTerm.isBlank()) {
            return List.of();
        }
        return itemSearchPort.searchByTitleOrDescription(searchTerm.trim());
    }
}