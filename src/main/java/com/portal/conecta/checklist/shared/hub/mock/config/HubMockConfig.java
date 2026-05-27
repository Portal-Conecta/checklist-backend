package com.portal.conecta.checklist.shared.hub.mock.config;

import com.portal.conecta.checklist.shared.hub.properties.HubMockProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"mock", "test"})
@EnableConfigurationProperties(HubMockProperties.class)
public class HubMockConfig {}
