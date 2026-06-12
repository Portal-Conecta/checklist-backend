package com.portal.conecta.checklist.modules.checklist.issues.application.port.out.persistence;

import com.portal.conecta.checklist.modules.checklist.issues.domain.model.ChecklistIssue;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.UUID;

public interface ChecklistIssueRepositoryPort extends ListCrudRepository<ChecklistIssue, UUID> {

    List<ChecklistIssue> findAllByChecklistExecution_Id(UUID executionId);
}
