package com.portal.conecta.checklist.module.checklist.application.port.out.persistence;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistSubmissionWindow;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface ChecklistSubmissionWindowRepositoryPort
        extends ListCrudRepository<ChecklistSubmissionWindow, UUID> {

    Optional<ChecklistSubmissionWindow> findByClassIdAndChecklistType(
            UUID classId,
            ChecklistType checklistType
    );

    List<ChecklistSubmissionWindow> findAllByClassIdOrderByChecklistTypeAsc(UUID classId);

    List<ChecklistSubmissionWindow> findAllByOrderByClassIdAscChecklistTypeAsc();
}
