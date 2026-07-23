package com.portal.conecta.checklist.shared.integration.hub.adapter.room;

import com.portal.conecta.checklist.module.checklist.application.port.out.integration.HubRoomProvider;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.RoomReference;
import com.portal.conecta.checklist.shared.integration.hub.client.room.HubBulkRoomRequest;
import com.portal.conecta.checklist.shared.integration.hub.client.room.HubBulkRoomResponse;
import com.portal.conecta.checklist.shared.integration.hub.client.room.HubRoomClient;
import com.portal.conecta.checklist.shared.integration.hub.client.room.HubRoomResponse;
import com.portal.conecta.checklist.shared.integration.hub.exception.HubIntegrationException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Provider HTTP para consultar salas no Hub real.
 *
 * <p>Usado por regras que precisam confirmar a existencia de uma sala antes de
 * persistir templates ou execucoes.</p>
 */
@Component
@RequiredArgsConstructor
public class HttpHubRoomProvider implements HubRoomProvider {

    private final HubRoomClient hubRoomClient;



    @Override
    public boolean existsById(UUID roomId) {
        return findById(roomId).isPresent();
    }

    @Override
    public Optional<RoomReference> findById(UUID roomId) {
        try {
            HubRoomResponse response = hubRoomClient.findById(roomId);

            return response == null ? Optional.empty() : Optional.of(toReference(response, roomId));
        } catch (FeignException.NotFound exception) {
            return Optional.empty();
        } catch (FeignException exception) {
            throw new HubIntegrationException("Servico de salas do Hub indisponivel.", exception);
        }
    }

    @Override
    public List<RoomReference> findByIds(List<UUID> roomIds) {
        if (roomIds == null || roomIds.isEmpty()) {
            return List.of();
        }
        List<UUID> uniqueIds = roomIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (uniqueIds.isEmpty()) {
            return List.of();
        }
        try {
            HubBulkRoomResponse response = hubRoomClient.findBulk(new HubBulkRoomRequest(uniqueIds));
            if (response == null || response.items() == null) {
                return List.of();
            }
            return response.items().stream()
                    .map(item -> toReference(item, item.id()))
                    .toList();
        } catch (FeignException exception) {
            throw new HubIntegrationException("Servico de salas do Hub indisponivel.", exception);
        }
    }

    private RoomReference toReference(HubRoomResponse response, UUID requestedRoomId) {
        UUID roomId = response.id() == null ? requestedRoomId : response.id();
        return new RoomReference(roomId, response.number(), response.typeRoom(), response.status());
    }
}
