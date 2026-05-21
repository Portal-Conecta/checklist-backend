package com.portal.conecta.checklist.shared.hub.mock;

import com.portal.conecta.checklist.shared.hub.dto.UserDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HubUserMockTest {

    private final HubUserMock hubUserMock = new HubUserMock();

    private static final UUID USER_KAEL   = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID USER_MURILO = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID USER_DANIEL = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID UNKNOWN     = UUID.fromString("99999999-9999-9999-9999-999999999999");

    @Test
    @DisplayName("Deve retornar usuário Kael com role REPRESENTANTE")
    void deveRetornarKael() {
        UserDTO user = hubUserMock.findById(USER_KAEL);

        assertNotNull(user);
        assertEquals(USER_KAEL, user.id());
        assertEquals("Kael", user.name());
        assertEquals("kael@conecta.com", user.email());
        assertEquals("REPRESENTANTE", user.typeUser());
    }

    @Test
    @DisplayName("Deve retornar usuário Murilo com role DOCENTE")
    void deveRetornarMurilo() {
        UserDTO user = hubUserMock.findById(USER_MURILO);

        assertNotNull(user);
        assertEquals(USER_MURILO, user.id());
        assertEquals("Murilo", user.name());
        assertEquals("DOCENTE", user.typeUser());
    }

    @Test
    @DisplayName("Deve retornar usuário Daniel com role ADMINISTRADOR")
    void deveRetornarDaniel() {
        UserDTO user = hubUserMock.findById(USER_DANIEL);

        assertNotNull(user);
        assertEquals(USER_DANIEL, user.id());
        assertEquals("Daniel", user.name());
        assertEquals("ADMINISTRADOR", user.typeUser());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException para UUID desconhecido")
    void deveLancarExcecaoParaUUIDDesconhecido() {
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> hubUserMock.findById(UNKNOWN)
        );

        assertTrue(exception.getMessage().contains(UNKNOWN.toString()));
    }
}
