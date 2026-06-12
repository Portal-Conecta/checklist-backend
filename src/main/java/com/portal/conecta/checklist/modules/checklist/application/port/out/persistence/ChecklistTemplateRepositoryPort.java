package com.portal.conecta.checklist.modules.checklist.application.port.out.persistence;

import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.UUID;

public interface ChecklistTemplateRepositoryPort extends ListCrudRepository<ChecklistTemplate, UUID> {

    List<ChecklistTemplate> findAllByActiveTrueAndStatus(ChecklistTemplateStatus status);

    List<ChecklistTemplate> findByTemplateGroupIdAndStatus(
            UUID templateGroupId,
            ChecklistTemplateStatus status
    );
}
