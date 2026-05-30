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

/**
 * consulta no banco dados relacionados á entidade ChecklistExecution
 */
    @Repository
    public interface ChecklistExecutionRepository extends JpaRepository<ChecklistExecution, UUID> {
    /**
     * verifica se já existe uma execução de checklist com os mesmos dados principais no mesmo dia
     * @param classId indentificação de uma turma
     * @param roomId indentificação de uma sala
     * @param period período da checklist
     * @param checklistType tipo da checklist
     * @param startOfDay início do intervalo do dia
     * @param endOfDay fim do intervalo do dia
     * @return true caso exista checklist duplicadas
     */
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
    /**
     * verifica se um usuário ficou três dias consecutivos sem enviar checklists
     * @param userId indentificação de um usuário
     * @return true caso não existam submissões nos últimos 3 dias consecutivos
     */
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

    /**
     *Busca todas as execuções de checklist de uma turma em um intervalo de data
     * @param classId indentificação de uma turma
     * @param startOfDay início do intervalo do dia
     * @param endOfDay fim do intervalo do dia
     * @return lista de execuções encontradas
     */
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

    /**
     *Verifica se existem itens não conformes sem justificativa preenchida.
     * @param executionId indentificação de uma execução de checklist
     * @return true caso exista item não conforme sem justificativa
     */
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
