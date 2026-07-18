package com.portal.conecta.checklist.module.issues.application.port.out.persistence;

import com.portal.conecta.checklist.module.issues.domain.model.ChecklistIssue;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.UUID;

@NoRepositoryBean
public interface ChecklistIssueRepositoryPort extends ListCrudRepository<ChecklistIssue, UUID> {

    List<ChecklistIssue> findAllByExecutionId(UUID executionId);
}
