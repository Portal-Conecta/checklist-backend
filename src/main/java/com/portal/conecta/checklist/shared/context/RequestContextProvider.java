package com.portal.conecta.checklist.shared.context;

/**
 * Contrato para obter o contexto do usuario autenticado.
 *
 * <p>Permite que use cases consumam dados de seguranca sem depender diretamente
 * de APIs do Spring Security ou de mocks de teste.</p>
 */
public interface RequestContextProvider {

    RequestContext getRequestContext();
}
