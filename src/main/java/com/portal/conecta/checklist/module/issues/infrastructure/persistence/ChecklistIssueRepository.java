package com.portal.conecta.checklist.module.issues.infrastructure.persistence;

import com.portal.conecta.checklist.module.issues.domain.model.ChecklistIssue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChecklistIssueRepository extends JpaRepository<ChecklistIssue, UUID> {

    List<ChecklistIssue> findAllByChecklistExecution_Id(UUID executionId);
}
