package com.portal.conecta.checklist.modules.checklist.issues.infrastructure.persistence;

import com.portal.conecta.checklist.modules.checklist.issues.domain.model.ChecklistIssue;
import com.portal.conecta.checklist.modules.checklist.issues.application.port.out.persistence.ChecklistIssueRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChecklistIssueRepository extends JpaRepository<ChecklistIssue, UUID>, ChecklistIssueRepositoryPort {

    List<ChecklistIssue> findAllByChecklistExecution_Id(UUID executionId);
}
