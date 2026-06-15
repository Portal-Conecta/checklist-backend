package com.portal.conecta.checklist.modules.checklist.application.usecase.window.query;

import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistSubmissionWindow;
import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistSubmissionWindowRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListSubmissionWindowsUseCase {

    private final ChecklistSubmissionWindowRepositoryPort repository;

    @Transactional(readOnly = true)
    public List<ChecklistSubmissionWindow> execute() {
        return repository.findAllByOrderByClassIdAscChecklistTypeAsc();
    }

    @Transactional(readOnly = true)
    public List<ChecklistSubmissionWindow> execute(UUID classId) {
        return repository.findAllByClassIdOrderByChecklistTypeAsc(classId);
    }
}
