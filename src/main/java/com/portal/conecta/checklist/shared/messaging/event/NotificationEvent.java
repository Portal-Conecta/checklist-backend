package com.portal.conecta.checklist.shared.messaging.event;

import java.util.List;
import java.util.UUID;

public record NotificationEvent(
        String messageId,
        String correlationId,
        String source,
        String eventType,
        String title,
        String body,
        List<UUID> recipientIds
) {}