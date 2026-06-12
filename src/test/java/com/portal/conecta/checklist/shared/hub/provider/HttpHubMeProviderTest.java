package com.portal.conecta.checklist.shared.hub.provider;

import com.portal.conecta.checklist.shared.hub.client.me.HubMeClient;
import com.portal.conecta.checklist.shared.hub.client.user.HubMyListCourseResponse;
import com.portal.conecta.checklist.shared.hub.provider.me.HttpHubMeProvider;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HttpHubMeProviderTest {

    private final HubMeClient hubMeClient = mock(HubMeClient.class);
    private final HttpHubMeProvider provider = new HttpHubMeProvider(hubMeClient);

    @Test
    void shouldValidateAuthenticatedUserWithMeCoursesEndpoint() {
        UUID tokenUserId = UUID.randomUUID();
        when(hubMeClient.findMyCourses()).thenReturn(new HubMyListCourseResponse(List.of()));

        assertTrue(provider.existsAuthenticatedUser(tokenUserId));
        verify(hubMeClient).findMyCourses();
    }
}
