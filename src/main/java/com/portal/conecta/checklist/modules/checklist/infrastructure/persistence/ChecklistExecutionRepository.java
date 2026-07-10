package com.portal.conecta.checklist.modules.checklist.infrastructure.persistence;

import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistExecutionRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChecklistExecutionRepository
        extends JpaRepository<ChecklistExecution, UUID>, ChecklistExecutionRepositoryPort {

    @Query(value = """
            select exists (
                select 1
                from checklist_execution ce
                where ce.class_id = :classId
                  and ce.room_id = :roomId
                  and ce.period = :period
                  and ce.checklist_type = :checklistType
                  and ce.started_at >= :startOfDay
                  and ce.started_at < :endOfDay
                  and ce.status <> 'CANCELED'
            )
            """, nativeQuery = true)
    boolean existsDuplicateChecklist(
            @Param("classId") UUID classId,
            @Param("roomId") UUID roomId,
            @Param("period") String period,
            @Param("checklistType") String checklistType,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    Page<ChecklistExecution> findByClassIdAndStatusOrderBySubmittedAtDesc(
            UUID classId,
            ChecklistExecutionStatus status,
            Pageable pageable
    );

    @Query(value = """
            with expected_days as (
                select (current_date - s)::date as day
                from generate_series(1, 3) s
            ),
            active_users as (
                select distinct user_id
                from checklist_execution
                where user_id is not null
                  and submitted_at >= current_date - interval '30 days'
            )
            select au.user_id
            from active_users au
            where (
                select count(*)
                from expected_days d
                where not exists (
                    select 1
                    from checklist_execution ce
                    where ce.user_id = au.user_id
                      and ce.status = 'SUBMITTED'
                      and ce.submitted_at >= d.day
                      and ce.submitted_at < d.day + interval '1 day'
                )
            ) = 3
            """, nativeQuery = true)
    List<UUID> findUsersWithThreeConsecutiveDaysWithoutSubmission();

    @Query(value = """
            select *
            from checklist_execution ce
            where ce.class_id = :classId
              and ce.started_at >= :startOfDay
              and ce.started_at < :endOfDay
            order by ce.started_at asc
            """, nativeQuery = true)
    List<ChecklistExecution> findByClassAndDateNative(
            @Param("classId") UUID classId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query(value = """
            select exists (
                select 1
                from checklist_execution ce
                cross join lateral jsonb_array_elements(ce.answers_json -> 'items') as item(value)
                where ce.id = :executionId
                  and (item.value ->> 'conforme')::boolean = false
                  and nullif(btrim(coalesce(item.value ->> 'justificativa', '')), '') is null
            )
            """, nativeQuery = true)
    boolean hasNonConformingItemWithoutJustification(
            @Param("executionId") UUID executionId
    );

    @Query(value = """
            select distinct csw.class_id as classId, csw.checklist_type as checklistType
            from checklist_submission_window csw
            where not exists (
                select 1
                from checklist_execution ce
                where ce.class_id = csw.class_id
                  and ce.checklist_type = csw.checklist_type
                  and ce.status = 'SUBMITTED'
                  and ce.submitted_at >= :startOfDay
                  and ce.submitted_at < :endOfDay
            )
            """, nativeQuery = true)
    List<MissedChecklistSummary> findClassIdsWithMissedChecklist(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );
}
