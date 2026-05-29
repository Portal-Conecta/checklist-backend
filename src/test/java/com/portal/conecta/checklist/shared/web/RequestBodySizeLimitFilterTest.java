package com.portal.conecta.checklist.shared.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RequestBodySizeLimitFilterTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void shouldRejectBodyAboveConfiguredLimit() throws Exception {
        RequestBodySizeLimitFilter filter = filter(3);
        MockHttpServletRequest request = postJsonRequest("abcd");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        filter.doFilter(request, response, (servletRequest, servletResponse) -> chainCalled.set(true));

        assertEquals(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, response.getStatus());
        assertFalse(chainCalled.get());
        assertThat(response.getContentAsString()).contains("limite maximo de 3 bytes");
    }

    @Test
    void shouldAllowBodyWithinLimitAndKeepItReadable() throws Exception {
        String content = "{\"ok\":true}";
        RequestBodySizeLimitFilter filter = filter(content.getBytes(StandardCharsets.UTF_8).length);
        MockHttpServletRequest request = postJsonRequest(content);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> bodyReadByChain = new AtomicReference<>();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                bodyReadByChain.set(new String(servletRequest.getInputStream().readAllBytes(), StandardCharsets.UTF_8))
        );

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals(content, bodyReadByChain.get());
    }

    private RequestBodySizeLimitFilter filter(long maxBodySizeBytes) {
        return new RequestBodySizeLimitFilter(new RequestBodyLimitProperties(maxBodySizeBytes), objectMapper);
    }

    private MockHttpServletRequest postJsonRequest(String content) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/checklist-templates");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent(content.getBytes(StandardCharsets.UTF_8));
        return request;
    }
}
