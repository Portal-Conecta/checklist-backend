package com.portal.conecta.checklist.module.checklist.infrastructure.persistense;

import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface ChecklistTemplateRepository extends JpaRepository<ChecklistTemplate,UUID > {



    @Query(value = "SELECT COUNT(*) " +
            "FROM checklist_template" +
            " WHERE room_id = :roomId" +
            " AND status = 'SUBMITTED' "
            , nativeQuery = true)
    Long countSubmittedActiveTemplatesByRoomId
            (@Param("roomId") UUID roomId);

    @Query(value = """
                SELECT id
                FROM checklist_template
                WHERE room_id = :roomId
                AND active = true 
                AND status = 'SUBMITTED'
                ORDER BY version Desc
                LIMIT 1
            """, nativeQuery = true)
    Optional<UUID> findLastestTemplateByRoomId(@Param("roomId")UUID roomId);

    @Query(value = """
            SELECT COUNT(id)
            FROM checklist_template
            WHERE room_id = :roomId
            AND active = true
            AND status = 'SUBMITTED'
            GROUP BY room_id
            HAVING COUNT(id) > 1
            
            """, nativeQuery = true)
    Long countActivAndSubmittedTemplatesByIdRoomId(@Param("roomid")UUID roomId);

    @Query(value = """
        SELECT *
        FROM checklist_template
        WHERE id = :templateId
        """, nativeQuery = true)
    Optional<ChecklistTemplate> findTemplateByIdNative(@Param("templateId") UUID templateId);




}
