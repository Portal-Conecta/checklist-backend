package com.portal.conecta.checklist.modules.checklist.application.port.out.persistence;

import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistCategory;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

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

    Page<ChecklistExecution> findByClassIdAndStatusAndCategoryOrderBySubmittedAtDesc(
            UUID classId,
            ChecklistExecutionStatus status,
            ChecklistCategory category,
            Pageable pageable
    );

    @Query(value = "SELECT COUNT(1) FROM checklist_execution WHERE user_id = :userId AND status = :status", nativeQuery = true)
    long countByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") String status);

    Page<ChecklistExecution> findAll(Pageable pageable);

    Page<ChecklistExecution> findByCategory(ChecklistCategory category, Pageable pageable);

    Page<ChecklistExecution> findByClassIdIn(Iterable<UUID> classIds, Pageable pageable);

    Page<ChecklistExecution> findByClassIdInAndCategory(
            Iterable<UUID> classIds,
            ChecklistCategory category,
            Pageable pageable
    );

}

