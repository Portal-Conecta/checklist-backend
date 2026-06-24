package com.portal.conecta.checklist.modules.checklist.application.port.out.persistence;

import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistItem;
import java.util.List;

public interface ChecklistItemSearchPort {
    List<ChecklistItem> searchByTitleOrDescription(String searchTerm);
}