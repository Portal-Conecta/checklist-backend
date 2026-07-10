package com.portal.conecta.checklist.modules.checklist.infrastructure.persistence;

import java.util.UUID;

public interface MissedChecklistSummary {
    UUID getClassId();
    String getChecklistType();
}
