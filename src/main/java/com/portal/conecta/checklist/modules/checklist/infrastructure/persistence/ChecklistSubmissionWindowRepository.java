package com.portal.conecta.checklist.modules.checklist.infrastructure.persistence;

import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistSubmissionWindow;
import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistSubmissionWindowRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChecklistSubmissionWindowRepository
        extends JpaRepository<ChecklistSubmissionWindow, UUID>, ChecklistSubmissionWindowRepositoryPort {

    Optional<ChecklistSubmissionWindow> findByClassIdAndChecklistType(UUID classId, ChecklistType checklistType);

    List<ChecklistSubmissionWindow> findAllByClassIdOrderByChecklistTypeAsc(UUID classId);

    List<ChecklistSubmissionWindow> findAllByOrderByClassIdAscChecklistTypeAsc();
}
