package com.portal.conecta.checklist.module.checklist.application.port.out.issue;

import java.util.UUID;

public record CreateNonComplianceIssueCommand(
        UUID executionId,
        UUID assignedUserId,
        String itemKey,
        String itemTitle,
        String observation
) {
}
