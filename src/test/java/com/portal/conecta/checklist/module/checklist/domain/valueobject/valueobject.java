package com.portal.conecta.checklist.module.checklist.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RoomReferenceTest {

    @Test
    @DisplayName("Deve criar RoomReference com sucesso quando o UUID for válido")
    void deveCriarRoomReferenceComSucesso() {
        UUID idValido = UUID.randomUUID();

        RoomReference roomReference = new RoomReference(idValido);

        assertNotNull(roomReference);
        assertEquals(idValido, roomReference.getRoomid());    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando o UUID for nulo")
    void deveLancarExcecaoQuandoUuidForNulo() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new RoomReference(null)
        );

        assertEquals("O ID da sala esta nulo", exception.getMessage());
    }
}