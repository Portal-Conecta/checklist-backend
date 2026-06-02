package com.portal.conecta.checklist.shared.hub.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

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
        List<String> roomIds
) {

    public HubMockProperties {
        classIds = classIds == null ? List.of() : List.copyOf(classIds);
        userIds = userIds == null ? List.of() : List.copyOf(userIds);
        roomIds = roomIds == null ? List.of() : List.copyOf(roomIds);
    }
}
