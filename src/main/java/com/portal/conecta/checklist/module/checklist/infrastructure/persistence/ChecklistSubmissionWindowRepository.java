package com.portal.conecta.checklist.module.checklist.infrastructure.persistence;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistSubmissionWindow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChecklistSubmissionWindowRepository extends JpaRepository<ChecklistSubmissionWindow, UUID> {

    Optional<ChecklistSubmissionWindow> findByClassIdAndChecklistType(UUID classId, ChecklistType checklistType);

    List<ChecklistSubmissionWindow> findAllByClassIdOrderByChecklistTypeAsc(UUID classId);

    List<ChecklistSubmissionWindow> findAllByOrderByClassIdAscChecklistTypeAsc();
}
