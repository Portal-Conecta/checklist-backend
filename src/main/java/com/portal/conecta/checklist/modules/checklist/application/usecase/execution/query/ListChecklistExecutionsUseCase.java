package com.portal.conecta.checklist.modules.checklist.application.usecase.execution.query;

import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistExecutionRepositoryPort;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.shared.context.ContextClass;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListChecklistExecutionsUseCase {

    private final ChecklistExecutionRepositoryPort repositoryPort;
    private final RequestContextProvider contextProvider;

    @Transactional(readOnly = true)
    public Page<ChecklistExecution> execute(Pageable pageable) {
        var context = contextProvider.getRequestContext();

        if (context.canManageChecklistTemplates()) {
            return repositoryPort.findAll(pageable);
        } else {
            List<UUID> classIds = context.classes().stream()
                    .map(ContextClass::classId)
                    .collect(Collectors.toList());
            
            if (classIds.isEmpty()) {
                return Page.empty(pageable);
            }
            
            return repositoryPort.findByClassIdIn(classIds, pageable);
        }
    }
}
