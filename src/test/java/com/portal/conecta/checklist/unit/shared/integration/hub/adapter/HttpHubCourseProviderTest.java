package com.portal.conecta.checklist.unit.shared.integration.hub.adapter;

import com.portal.conecta.checklist.shared.integration.hub.client.course.HubCourseClient;
import com.portal.conecta.checklist.shared.integration.hub.client.course.HubCourseResponse;
import com.portal.conecta.checklist.shared.integration.hub.adapter.course.HttpHubCourseProvider;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpHubCourseProviderTest {

    private final HubCourseClient hubCourseClient = mock(HubCourseClient.class);
    private final HttpHubCourseProvider provider = new HttpHubCourseProvider(hubCourseClient);

    @Test
    void shouldTranslateHubCourseResponseToCourseReference() {
        UUID courseId = UUID.randomUUID();

        when(hubCourseClient.findById(courseId)).thenReturn(new HubCourseResponse(
                courseId,
                "Desenvolvimento de Sistemas",
                "DEV-01"
        ));

        var reference = provider.findById(courseId);

        assertTrue(reference.isPresent());
        assertEquals(courseId, reference.orElseThrow().getCourseId());
        assertEquals("Desenvolvimento de Sistemas", reference.orElseThrow().getName());
        assertEquals("DEV-01", reference.orElseThrow().getCode());
    }
}
