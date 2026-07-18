package com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.submit;

import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;

public record ChecklistNonComplianceEvent(ChecklistExecution execution) {}
