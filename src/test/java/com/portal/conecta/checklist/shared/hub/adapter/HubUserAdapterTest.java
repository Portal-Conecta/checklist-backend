package com.portal.conecta.checklist.shared.hub.adapter;

import com.portal.conecta.checklist.shared.hub.client.HubUserClient;
import com.portal.conecta.checklist.shared.hub.dto.UserDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HubUserAdapterTest {

    private final HubUserClient hubUserClient = mock(HubUserClient.class);
    private final HubUserAdapter adapter = new HubUserAdapter(hubUserClient);

    @Test
    @DisplayName("Deve retornar UserDTO ao delegar para o cliente Feign")
    void deveRetornarUserDTOAoDelegarParaCliente() {
        UUID userId = UUID.randomUUID();
        UserDTO expected = new UserDTO(userId, "Daniel", "daniel@conecta.com", null, "ADMINISTRADOR");

        when(hubUserClient.findById(userId)).thenReturn(expected);

        UserDTO result = adapter.findById(userId);

        assertNotNull(result);
        assertEquals(expected, result);
        verify(hubUserClient, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Deve propagar EntityNotFoundException quando o cliente lançar")
    void devePropagarEntityNotFoundExceptionDoCliente() {
        UUID userId = UUID.randomUUID();

        when(hubUserClient.findById(userId))
                .thenThrow(new EntityNotFoundException("Usuário não encontrado no Hub"));

        assertThrows(EntityNotFoundException.class, () -> adapter.findById(userId));
        verify(hubUserClient, times(1)).findById(userId);
    }
}
