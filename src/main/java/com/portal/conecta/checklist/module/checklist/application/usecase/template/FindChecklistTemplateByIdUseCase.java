package com.portal.conecta.checklist.module.checklist.application.usecase.template;

import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistTemplateRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindChecklistTemplateByIdUseCase {

    private final ChecklistTemplateRepository templateRepository;

    @Transactional(readOnly = true)
    public ChecklistTemplate execute(UUID templateId) {
        return templateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("Template de checklist nao encontrado."));
    }
}
