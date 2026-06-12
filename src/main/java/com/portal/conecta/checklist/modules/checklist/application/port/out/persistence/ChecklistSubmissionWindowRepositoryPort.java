package com.portal.conecta.checklist.modules.checklist.application.port.out.persistence;

import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistSubmissionWindow;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChecklistSubmissionWindowRepositoryPort
        extends ListCrudRepository<ChecklistSubmissionWindow, UUID> {

    Optional<ChecklistSubmissionWindow> findByClassIdAndChecklistType(
            UUID classId,
            ChecklistType checklistType
    );

    List<ChecklistSubmissionWindow> findAllByClassIdOrderByChecklistTypeAsc(UUID classId);

    List<ChecklistSubmissionWindow> findAllByOrderByClassIdAscChecklistTypeAsc();
}
