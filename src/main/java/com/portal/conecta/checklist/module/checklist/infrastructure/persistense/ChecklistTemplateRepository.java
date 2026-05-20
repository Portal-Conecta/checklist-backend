package com.portal.conecta.checklist.module.checklist.infrastructure.persistense;

import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import org.hibernate.validator.constraints.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

@Component
public interface ChecklistTemplateRepository extends JpaRepository<UUID, ChecklistTemplate> {



    @Query(value = "SELECT COUNT(*) " +
            "FROM checklist_template" +
            " WHERE room_id = :roomId" +
            " AND status = 'SUBMITTED' "
            , nativeQuery = true)
    Long countSubmittedActiveTemplatesByRoomId
            (@Param("roomId") UUID roomId);

    @Query(value = """
                SELECT()
            
            
            """, nativeQuery = true)
    ;




}
