package com.portal.conecta.checklist.module.checklist.application.usecase.window;

import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistSubmissionWindow;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistSubmissionWindowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListSubmissionWindowsUseCase {

    private final ChecklistSubmissionWindowRepository repository;

    @Transactional(readOnly = true)
    public List<ChecklistSubmissionWindow> execute() {
        return repository.findAllByOrderByShiftAscChecklistTypeAsc();
    }
}
