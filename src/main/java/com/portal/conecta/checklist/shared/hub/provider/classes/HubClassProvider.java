package com.portal.conecta.checklist.shared.hub.provider.classes;

import java.util.UUID;

/**
 * Contrato para validar turmas conhecidas pelo Hub.
 *
 * <p>Isola a aplicacao da origem dos dados, permitindo usar implementacoes HTTP
 * em producao e mocks em testes.</p>
 */
public interface HubClassProvider {

    boolean existsById(UUID classId);
}
