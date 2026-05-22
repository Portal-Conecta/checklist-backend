package com.portal.conecta.checklist.module.checklist.application.usecase.template;

import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListChecklistTemplatesUseCase {

    private final ChecklistTemplateRepository templateRepository;

    @Transactional(readOnly = true)
    public List<ChecklistTemplate> execute() {
        return templateRepository.findAll();
    }
}
