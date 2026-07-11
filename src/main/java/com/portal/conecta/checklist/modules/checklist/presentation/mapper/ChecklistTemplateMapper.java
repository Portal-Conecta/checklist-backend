package com.portal.conecta.checklist.modules.checklist.presentation.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.modules.checklist.application.port.out.integration.HubRoomProvider;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistItem;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSchema;
import com.portal.conecta.checklist.modules.checklist.domain.valueobject.RoomReference;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.shared.RoomResponseDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.template.request.ChecklistTemplateCreateRequest;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.template.response.ChecklistItemSearchResponseDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.template.response.ChecklistTemplateResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ChecklistTemplateMapper {

    private final ObjectMapper objectMapper;
    private final HubRoomProvider hubRoomProvider;

    public ChecklistTemplateMapper(ObjectMapper objectMapper, HubRoomProvider hubRoomProvider) {
        this.objectMapper = objectMapper;
        this.hubRoomProvider = hubRoomProvider;
    }

    public ChecklistTemplateResponseDTO toResponseWithEnrichment(ChecklistTemplate template) {
        if (template == null) {
            return null;
        }
        RoomReference roomRef = null;
        try {
            roomRef = hubRoomProvider.findById(template.getRoomId()).orElse(null);
        } catch (Exception e) {
            log.warn("Falha ao buscar sala no Hub para template {}: {}", template.getId(), e.getMessage());
        }
        return toResponse(template, roomRef);
    }

    public List<ChecklistTemplateResponseDTO> toResponseListWithEnrichment(List<ChecklistTemplate> templates) {
        if (templates == null) {
            return List.of();
        }
        List<UUID> roomIds = templates.stream()
                .map(template -> template.getRoomId())
                .toList();

        Map<UUID, RoomReference> roomMap = Map.of();
        try {
            List<RoomReference> rooms = hubRoomProvider.findByIds(roomIds);
            roomMap = rooms.stream()
                    .collect(Collectors.toMap(room -> room.getRoomId(), room -> room, (r1, r2) -> r1));
        } catch (Exception e) {
            log.warn("Falha ao buscar salas no Hub para lista de templates: {}", e.getMessage());
        }

        return toResponseList(templates, roomMap);
    }

    public ChecklistTemplateResponseDTO toResponse(ChecklistTemplate template) {
        return toResponse(template, null);
    }

    public ChecklistTemplateResponseDTO toResponse(ChecklistTemplate template, RoomReference room) {
        if (template == null) {
            return null;
        }

        RoomResponseDTO roomDTO = null;
        if (room != null) {
            roomDTO = new RoomResponseDTO(
                    room.getRoomId(),
                    room.getNumber(),
                    room.getTypeRoom(),
                    room.getStatus()
            );
        }

        return new ChecklistTemplateResponseDTO(
                template.getId(),
                template.getRoomId(),
                template.getTitle(),
                template.getDescription(),
                template.getVersion(),
                template.getStatus(),
                template.isActive(),
                toSchema(template.getSchemaJson()),
                template.getCreatedAt(),
                template.getUpdatedAt(),
                roomDTO
        );
    }

    public List<ChecklistTemplateResponseDTO> toResponseList(List<ChecklistTemplate> templates) {
        return toResponseList(templates, Map.of());
    }

    public List<ChecklistTemplateResponseDTO> toResponseList(List<ChecklistTemplate> templates, Map<UUID, RoomReference> roomMap) {
        if (templates == null) {
            return List.of();
        }
        return templates.stream()
                .map(t -> toResponse(t, roomMap != null ? roomMap.get(t.getRoomId()) : null))
                .toList();
    }

    public ChecklistSchema toSchema(Map<String, Object> schemaJson) {
        return schemaJson == null || schemaJson.isEmpty()
                ? new ChecklistSchema(List.of())
                : objectMapper.convertValue(schemaJson, ChecklistSchema.class);
    }

    public ChecklistTemplate toEntity(ChecklistTemplateCreateRequest request) {
        if (request == null) {
            return null;
        }

        ChecklistTemplate template = new ChecklistTemplate();
        template.setRoomId(request.roomId());
        template.setTitle(request.title());
        template.setDescription(request.description());

        if (request.schemaJson() != null) {
            Map<String, Object> mappedSchema = objectMapper.convertValue(
                    request.schemaJson(),
                    new TypeReference<Map<String, Object>>() {}
            );
            template.setSchemaJson(mappedSchema);
        }

        template.setActive(true);

        return template;
    }

    public ChecklistItemSearchResponseDTO toItemSearchResponseDTO(ChecklistItem item){
        if (item == null) return null;

        return new ChecklistItemSearchResponseDTO(
                item.key(),
                item.title(),
                item.description(),
                item.required(),
                item.order()
        );
    }

}
