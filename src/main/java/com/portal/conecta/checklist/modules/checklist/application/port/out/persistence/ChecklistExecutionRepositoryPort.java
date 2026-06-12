package com.portal.conecta.checklist.modules.checklist.application.port.out.persistence;

import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.ListCrudRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@NoRepositoryBean
public interface ChecklistExecutionRepositoryPort extends ListCrudRepository<ChecklistExecution, UUID> {

    boolean existsDuplicateChecklist(
            UUID classId,
            UUID roomId,
            String period,
            String checklistType,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );

    Page<ChecklistExecution> findByClassIdAndStatusOrderBySubmittedAtDesc(
            UUID classId,
            ChecklistExecutionStatus status,
            Pageable pageable
    );
}
