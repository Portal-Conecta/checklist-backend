package com.portal.conecta.checklist.modules.checklist.application.port.out.messaging;

import com.portal.conecta.checklist.shared.messaging.event.NotificationEvent;

public interface NotificationEventPublisher {
    void publish(NotificationEvent event);
}