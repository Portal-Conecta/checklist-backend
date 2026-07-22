package com.portal.conecta.checklist.module.issues.infrastructure.persistence;

import com.portal.conecta.checklist.module.issues.domain.model.ChecklistIssue;
import com.portal.conecta.checklist.module.issues.application.port.out.persistence.ChecklistIssueRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChecklistIssueRepository extends JpaRepository<ChecklistIssue, UUID>, ChecklistIssueRepositoryPort {

    List<ChecklistIssue> findAllByExecutionId(UUID executionId);
}
