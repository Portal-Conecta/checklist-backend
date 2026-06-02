package com.portal.conecta.checklist.shared.context.provider;

import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.context.TypeUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Provider de contexto autenticado usado em testes.
 *
 * <p>Cria um {@link RequestContext} fixo a partir de propriedades mockadas,
 * evitando dependencia do filtro JWT em testes de aplicacao.</p>
 */
@Component
@Profile("test")
public class MockRequestContextProvider implements RequestContextProvider {

    private final UUID id;
    private final TypeUser userType;

    public MockRequestContextProvider(
            @Value("${checklist.mock.current-user.id:44444444-4444-4444-4444-444444444444}") String id,
            @Value("${checklist.mock.current-user.type:SENAI}") String userType
    ) {
        this.id = UUID.fromString(id);
        this.userType = TypeUser.valueOf(userType);
    }

    @Override
    public RequestContext getRequestContext() {
        return new RequestContext(id, userType, List.of());
    }
}
