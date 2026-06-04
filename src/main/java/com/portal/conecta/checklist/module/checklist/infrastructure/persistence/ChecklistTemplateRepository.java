package com.portal.conecta.checklist.module.checklist.infrastructure.persistence;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChecklistTemplateRepository extends JpaRepository<ChecklistTemplate, UUID> {

    @Query(value = "SELECT COUNT(*) " +
            "FROM checklist_template" +
            " WHERE room_id = :roomId" +
            " AND active = true" +
            " AND status = 'ACTIVE' "
            , nativeQuery = true)
    Long countActiveTemplatesByRoomId(@Param("roomId") UUID roomId);

    @Query(value = """
                SELECT id
                FROM checklist_template
                WHERE room_id = :roomId
                AND active = true
                AND status = 'ACTIVE'
                ORDER BY version Desc
                LIMIT 1
            """, nativeQuery = true)
    Optional<UUID> findLatestActiveTemplateByRoomId(@Param("roomId") UUID roomId);

    @Query(value = """
            SELECT COUNT(id)
            FROM checklist_template
            WHERE room_id = :roomId
            AND active = true
            AND status = 'ACTIVE'
            GROUP BY room_id
            HAVING COUNT(id) > 1
            
            """, nativeQuery = true)
    Long countActiveTemplateConflictsByRoomId(@Param("roomId") UUID roomId);

    @Query(value = """
        SELECT *
        FROM checklist_template
        WHERE id = :templateId
        """, nativeQuery = true)
    Optional<ChecklistTemplate> findTemplateByIdNative(@Param("templateId") UUID templateId);

    List<ChecklistTemplate> findByRoomIdAndActiveTrueAndStatus(UUID roomId, ChecklistTemplateStatus status);
}
