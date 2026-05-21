package com.portal.conecta.checklist.module.issues.domain.model;

import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ChecklistIssueTest {

    @Test
    @DisplayName("deve resolver issue atualizando status e resolvedAt")
    void deveResolverIssueAtualizandoStatusEResolvedAt() {
        ChecklistIssue issue = ChecklistIssue.builder().build();

        issue.resolve();

        assertEquals(Status.RESOLVED, issue.getStatus());
        assertNotNull(issue.getResolvedAt());
    }

    @Test
    @DisplayName("deve vincular issue ao checklist execution")
    void deveVincularIssueAoChecklistExecution() {
        ChecklistExecution execution = ChecklistExecution.builder().build();
        ChecklistIssue issue = ChecklistIssue.builder().build();

        execution.addIssue(issue);

        assertEquals(1, execution.getIssues().size());
        assertSame(issue, execution.getIssues().get(0));
        assertSame(execution, issue.getChecklistExecution());
    }
}
