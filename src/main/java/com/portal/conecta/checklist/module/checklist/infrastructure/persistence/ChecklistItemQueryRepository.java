package com.portal.conecta.checklist.module.checklist.infrastructure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.module.checklist.application.port.out.persistence.ChecklistItemSearchPort;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.domain.schema.ChecklistItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ChecklistItemQueryRepository implements ChecklistItemSearchPort {

    private final ChecklistTemplateRepository checklistTemplateRepository;
    private final ObjectMapper objectMapper;

    @Override
    @SuppressWarnings("unchecked")
    public List<ChecklistItem> searchByTitleOrDescription(String searchTerm) {
        List<ChecklistTemplate> activeTemplates = checklistTemplateRepository
                .findAllByActiveTrueAndStatus(ChecklistTemplateStatus.ACTIVE);

        List<ChecklistItem> matchedItems = new ArrayList<>();
        String lowerTerm = searchTerm.toLowerCase();

        for (ChecklistTemplate template : activeTemplates) {
            Map<String, Object> schema = template.getSchemaJson();
            if (schema == null || !schema.containsKey("sections")) continue;

            List<Map<String, Object>> sections = (List<Map<String, Object>>) schema.get("sections");
            if (sections == null) continue;

            for (Map<String, Object> section : sections) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) section.get("items");
                if (items == null) continue;

                for (Map<String, Object> itemMap : items) {
                    ChecklistItem item = objectMapper.convertValue(itemMap, ChecklistItem.class);

                    boolean matchesTitle = item.title() != null && item.title().toLowerCase().contains(lowerTerm);
                    boolean matchesDesc = item.description() != null && item.description().toLowerCase().contains(lowerTerm);

                    if (matchesTitle || matchesDesc) {
                        matchedItems.add(item);
                    }
                }
            }
        }
        return matchedItems;
    }
}