package com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.submit;

import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistExecution;

public record ChecklistNonComplianceEvent(ChecklistExecution execution) {}
