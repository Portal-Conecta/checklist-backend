package com.portal.conecta.checklist.module.checklist.infrastructure.persistence;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChecklistExecutionRepository extends JpaRepository<ChecklistExecution, UUID> {

    List<ChecklistExecution> findByClassIdAndStatusOrderBySubmittedAtDesc(
            UUID classId,
            ChecklistExecutionStatus status
    );

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

    @Query(value = """
            with expected_days as (
                select (current_date - s)::date as day
                from generate_series(1, 3) s
            )
            select count(*) = 3
            from expected_days d
            where not exists (
                select 1
                from checklist_execution ce
                where ce.user_id = :userId
                  and ce.status = 'SUBMITTED'
                  and ce.submitted_at >= d.day
                  and ce.submitted_at < d.day + interval '1 day'
            )
            """, nativeQuery = true)
    boolean hasThreeConsecutiveDaysWithoutSubmission(
            @Param("userId") UUID userId
    );

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



}
