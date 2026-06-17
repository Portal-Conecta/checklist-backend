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

/**
 * Repositório responsável pelo acesso às execuções de checklist persistidas.
 */
@Repository
public interface ChecklistExecutionRepository
        extends JpaRepository<ChecklistExecution, UUID>, ChecklistExecutionRepositoryPort {

    /**
     * Verifica se já existe uma execução de checklist com os mesmos dados principais no mesmo dia.
     *
     * @param classId identificação de uma turma.
     * @param roomId identificação de uma sala.
     * @param period período da checklist.
     * @param checklistType tipo da checklist.
     * @param startOfDay início do intervalo do dia.
     * @param endOfDay fim do intervalo do dia.
     * @return {@code true} caso exista checklist duplicada.
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

    /**
     * Busca execuções de uma turma filtradas por status, ordenando as mais recentes primeiro.
     *
     * @param classId identificador único da turma.
     * @param status status usado como filtro da consulta.
     * @return lista de execuções encontradas para a turma e status informados.
     */
    Page<ChecklistExecution> findByClassIdAndStatusOrderBySubmittedAtDesc(
            UUID classId,
            ChecklistExecutionStatus status,
            Pageable pageable
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


        @Query(value = """
                SELECT COUNT(1)
                    FROM checklist_execution
                    WHERE user_id = :userId
                    AND status = :status
                """,nativeQuery = true)
        long countByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") String status);



    }
