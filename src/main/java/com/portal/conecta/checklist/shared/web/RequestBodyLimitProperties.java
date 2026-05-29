package com.portal.conecta.checklist.shared.web;

import com.portal.conecta.checklist.module.checklist.domain.validation.ChecklistTemplateLimits;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "checklist.request")
public record RequestBodyLimitProperties(Long maxBodySizeBytes) {

    public RequestBodyLimitProperties {
        maxBodySizeBytes = maxBodySizeBytes == null
                ? ChecklistTemplateLimits.MAX_REQUEST_BODY_BYTES
                : maxBodySizeBytes;

        if (maxBodySizeBytes <= 0) {
            throw new IllegalStateException("checklist.request.max-body-size-bytes deve ser maior que zero.");
        }
    }
}
