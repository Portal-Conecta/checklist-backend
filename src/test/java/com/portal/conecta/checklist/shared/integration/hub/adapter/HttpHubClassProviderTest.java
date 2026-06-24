package com.portal.conecta.checklist.shared.integration.hub.adapter;

import com.portal.conecta.checklist.modules.checklist.domain.enums.Shift;
import com.portal.conecta.checklist.modules.checklist.domain.valueobject.ClassReference;
import com.portal.conecta.checklist.shared.integration.hub.adapter.classes.HttpHubClassProvider;
import com.portal.conecta.checklist.shared.integration.hub.client.classes.HubBulkClassRequest;
import com.portal.conecta.checklist.shared.integration.hub.client.classes.HubBulkClassResponse;
import com.portal.conecta.checklist.shared.integration.hub.client.classes.HubClassClient;
import com.portal.conecta.checklist.shared.integration.hub.client.classes.HubClassResponse;
import com.portal.conecta.checklist.shared.integration.hub.exception.HubIntegrationException;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Test
    @DisplayName("findByIds deve retornar lista de ClassReference para IDs encontrados")
    void findByIds_shouldReturnClassReferences() {
        UUID classId1 = UUID.randomUUID();
        UUID classId2 = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-06-09T18:00:00Z");

        List<HubClassResponse> items = List.of(
                new HubClassResponse(classId1, Shift.FULL_AM_PM, 77, "Turma 77", courseId, createdAt),
                new HubClassResponse(classId2, Shift.FULL_PM_NT, 88, "Turma 88", courseId, createdAt)
        );
        HubBulkClassResponse bulkResponse = new HubBulkClassResponse(items, List.of(classId1, classId2), List.of());

        when(hubClassClient.findBulk(any(HubBulkClassRequest.class))).thenReturn(bulkResponse);

        List<ClassReference> result = provider.findByIds(List.of(classId1, classId2));

        assertEquals(2, result.size());
        assertEquals(classId1, result.get(0).getClassId());
        assertEquals("Turma 77", result.get(0).getName());
        assertEquals(classId2, result.get(1).getClassId());
        assertEquals("Turma 88", result.get(1).getName());
    }

    @Test
    @DisplayName("findByIds com lista vazia deve retornar lista vazia sem chamar Hub")
    void findByIds_withEmptyList_shouldReturnEmpty() {
        List<ClassReference> result = provider.findByIds(List.of());

        assertTrue(result.isEmpty());
        verifyNoInteractions(hubClassClient);
    }

    @Test
    @DisplayName("findByIds deve deduplicar IDs antes de chamar Hub")
    void findByIds_shouldDeduplicateIds() {
        UUID classId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        HubBulkClassResponse bulkResponse = new HubBulkClassResponse(
                List.of(new HubClassResponse(classId, Shift.FULL_PM_NT, 1, "Turma 1", courseId, createdAt)),
                List.of(classId),
                List.of()
        );

        when(hubClassClient.findBulk(any(HubBulkClassRequest.class))).thenReturn(bulkResponse);

        List<ClassReference> result = provider.findByIds(List.of(classId, classId, classId));

        assertEquals(1, result.size());
        verify(hubClassClient, times(1)).findBulk(any(HubBulkClassRequest.class));
    }

    @Test
    @DisplayName("findByIds deve lancar HubIntegrationException em falha de comunicacao")
    void findByIds_shouldThrowHubIntegrationExceptionOnFeignError() {
        UUID classId = UUID.randomUUID();

        when(hubClassClient.findBulk(any(HubBulkClassRequest.class)))
                .thenThrow(mock(FeignException.class));

        assertThrows(HubIntegrationException.class, () -> provider.findByIds(List.of(classId)));
    }
}

