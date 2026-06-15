package com.portal.conecta.checklist.shared.integration.hub.mock.config;

import com.portal.conecta.checklist.shared.integration.hub.config.HubMockProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuracao dos mocks de integracao com o Hub.
 *
 * <p>Habilita propriedades locais usadas pelos providers mockados nos profiles
 * {@code mock} e {@code test}.</p>
 */
@Configuration
@Profile({"mock", "test"})
@EnableConfigurationProperties(HubMockProperties.class)
public class HubMockConfig {}
