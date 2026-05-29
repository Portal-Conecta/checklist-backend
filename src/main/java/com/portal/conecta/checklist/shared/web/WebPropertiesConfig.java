package com.portal.conecta.checklist.shared.web;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RequestBodyLimitProperties.class)
public class WebPropertiesConfig {
}
