package com.portal.conecta.checklist.shared.hub.provider.room;

import java.util.UUID;

/**
 * Contrato para validar salas conhecidas pelo Hub.
 *
 * <p>Permite que o modulo Checklist dependa apenas de um identificador de sala,
 * mantendo os dados completos centralizados no Hub.</p>
 */
public interface HubRoomProvider {

    boolean existsById(UUID roomId);
}
