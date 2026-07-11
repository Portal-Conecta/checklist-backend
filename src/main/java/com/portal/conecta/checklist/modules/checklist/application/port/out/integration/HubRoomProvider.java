package com.portal.conecta.checklist.modules.checklist.application.port.out.integration;

import com.portal.conecta.checklist.modules.checklist.domain.valueobject.RoomReference;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrato para validar salas conhecidas pelo Hub.
 *
 * <p>Permite que o modulo Checklist dependa apenas de um identificador de sala,
 * mantendo os dados completos centralizados no Hub.</p>
 */
public interface HubRoomProvider {

    boolean existsById(UUID roomId);

    default Optional<RoomReference> findById(UUID roomId) {
        return existsById(roomId)
                ? Optional.of(new RoomReference(roomId))
                : Optional.empty();
    }

    List<RoomReference> findByIds(List<UUID> roomIds);
}
