package com.portal.conecta.checklist.module.checklist.infrastructure.persistense;

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

    @Query(value = """
            select exists (
                select 1
                from checklist_execution ce
                where ce.class_id = :classId
                  and ce.started_at >= :startOfDay
                  and ce.started_at < :endOfDay
                  and ce.checklist_type = :checklistType
                  and ce.status <> 'CANCELED'
            )
            """, nativeQuery = true)
    boolean existsDuplicateChecklist(
            @Param("classId") UUID classId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            @Param("checklistType") String checklistType
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
        SELECT 
            elem->>'itemKey' AS itemKey,
            elem->>'observation' AS observation
        FROM checklist_execution ce,
        LATERAL jsonb_array_elements(ce.answers_json -> 'answers') AS elem
        WHERE ce.id = :executionId
          AND ce.status = 'SUBMITTED'
          AND elem->>'value' = 'NON_COMPLIANT'
        """, nativeQuery = true)
    List<Object[]> findNonCompliantItemsToCreateIssues(
            @Param("executionId") UUID executionId
    );
    @Query(value = """
            SELECT EXISTS(
            SELECT 1 FROM checklist_execution 
            WHERE class_id = :classId
            AND room_id = :roomId
            AND period = :period
            AND checklist_type = :checklist_type
            AND DATE(started_at) = CURRENT_DATE
            AND status IN ('DRAFT','SUBMITTED')
            )
            """, nativeQuery = true)
    boolean hasArrivalToday(@Param("classId")UUID classId,
                            @Param("roomId")UUID roomId,
                            @Param("period")Period period);




}
