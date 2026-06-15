package com.portal.conecta.checklist.modules.checklist.infrastructure.persistence;

import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistTemplateRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * consulta no banco dados relacionados á entidade ChecklistTemplate
 */
@Repository
public interface ChecklistTemplateRepository
        extends JpaRepository<ChecklistTemplate, UUID>, ChecklistTemplateRepositoryPort {
    /**
     * @param roomId indentificação de uma sala
     * @return conta quantos checklists tem para uma sala
     */

    @Query(value = "SELECT COUNT(*) " +
            "FROM checklist_template" +
            " WHERE room_id = :roomId" +
            " AND active = true" +
            " AND status = 'ACTIVE' "
            , nativeQuery = true)
    Long countActiveTemplatesByRoomId(@Param("roomId") UUID roomId);

    /**
     * @param roomId indentificação de uma sala
     * @return busca o ID do checklist ativo da sala (mais recente)
     */
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

    /**
     * @param roomId indentificação de uma sala
     * @return verifica se existe várias checklists ativos na mesma sala
     */
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

    /**
     * @param templateId indentificação de uma sala
     * @return busca uma checklist pelo ID utilizando SQL nativo
     */
    @Query(value = """
        SELECT *
        FROM checklist_template
        WHERE id = :templateId
        """, nativeQuery = true)
    Optional<ChecklistTemplate> findTemplateByIdNative(@Param("templateId") UUID templateId);

    /**
     *
     * @param roomId indentificação de uma sala
     * @param status verifica os status da checklist
     * @return cria a query automaticamente pelo nome do método.
     */
    List<ChecklistTemplate> findByRoomIdAndActiveTrueAndStatus(UUID roomId, ChecklistTemplateStatus status);

    /**
     * @param templateGroupId identificador do grupo de versões do template
     * @param status status do template
     * @return lista de templates do mesmo grupo com o status informado
     */
    List<ChecklistTemplate> findByTemplateGroupIdAndStatus(UUID templateGroupId, ChecklistTemplateStatus status);
           
    List<ChecklistTemplate> findAllByActiveTrueAndStatus(ChecklistTemplateStatus status);
}
