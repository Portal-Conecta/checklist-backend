package com.portal.conecta.checklist.shared.hub.provider;

import com.portal.conecta.checklist.module.checklist.domain.enums.Shift;
import com.portal.conecta.checklist.shared.hub.client.classes.HubClassClient;
import com.portal.conecta.checklist.shared.hub.client.classes.HubClassResponse;
import com.portal.conecta.checklist.shared.hub.provider.classes.HttpHubClassProvider;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpHubClassProviderTest {

    private final HubClassClient hubClassClient = mock(HubClassClient.class);
    private final HttpHubClassProvider provider = new HttpHubClassProvider(hubClassClient);

    @Test
    void shouldTranslateHubClassResponseToClassReference() {
        UUID classId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-06-09T18:00:00Z");

        when(hubClassClient.findById(classId)).thenReturn(new HubClassResponse(
                classId,
                Shift.FULL_AM_PM,
                77,
                "Turma 77",
                courseId,
                createdAt
        ));

        var reference = provider.findById(classId);

        assertTrue(reference.isPresent());
        assertEquals(classId, reference.orElseThrow().getClassId());
        assertEquals("Turma 77", reference.orElseThrow().getName());
        assertEquals(77, reference.orElseThrow().getNumber());
        assertEquals(Shift.FULL_AM_PM, reference.orElseThrow().getShift());
        assertEquals(courseId, reference.orElseThrow().getCourseReference().getCourseId());
        assertEquals(createdAt, reference.orElseThrow().getCreatedAt());
    }
}
