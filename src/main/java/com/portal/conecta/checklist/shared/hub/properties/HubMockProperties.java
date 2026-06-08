package com.portal.conecta.checklist.shared.hub.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.List;
import java.util.Map;

/**
 * Propriedades com identificadores mockados do Hub.
 *
 * <p>Centraliza listas de usuarios, salas e turmas aceitas durante execucao
 * local com profile {@code mock} ou em testes.</p>
 */
@ConfigurationProperties(prefix = "checklist.mock.hub")
public record HubMockProperties(
        List<String> classIds,
        List<String> userIds,
        List<String> roomIds,
        List<String> courseIds,
        Map<String, String> classShifts
) {

    public HubMockProperties(List<String> classIds, List<String> userIds, List<String> roomIds) {
        this(classIds, userIds, roomIds, List.of(), Map.of());
    }

    public HubMockProperties(List<String> classIds, List<String> userIds, List<String> roomIds, List<String> courseIds) {
        this(classIds, userIds, roomIds, courseIds, Map.of());
    }

    @ConstructorBinding
    public HubMockProperties {
        classIds = classIds == null ? List.of() : List.copyOf(classIds);
        userIds = userIds == null ? List.of() : List.copyOf(userIds);
        roomIds = roomIds == null ? List.of() : List.copyOf(roomIds);
        courseIds = courseIds == null ? List.of() : List.copyOf(courseIds);
        classShifts = classShifts == null ? Map.of() : Map.copyOf(classShifts);
    }
}
