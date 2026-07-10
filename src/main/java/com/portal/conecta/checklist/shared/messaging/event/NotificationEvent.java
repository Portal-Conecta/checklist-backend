package com.portal.conecta.checklist.shared.messaging.event;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Event published by Checklist API to the Hub notification consumer.
 *
 * <p>The producer describes the notification intent, while Hub resolves the
 * final recipients from filters and scopes.</p>
 */
public record NotificationEvent(
        String messageId,
        String correlationId,
        String source,
        String eventType,
        Instant occurredAt,
        String title,
        String body,
        List<NotificationFilter> filters,
        List<NotificationScope> scope,
        Map<String, Object> metadata
) {

    public record NotificationFilter(
            String type,
            String value
    ) {
    }

    public record NotificationScope(
            String type,
            String correlationId
    ) {
    }
}
