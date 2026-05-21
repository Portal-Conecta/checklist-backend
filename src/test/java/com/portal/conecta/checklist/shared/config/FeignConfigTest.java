package com.portal.conecta.checklist.shared.config;

import com.portal.conecta.checklist.shared.hub.exception.HubIntegrationException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import feign.codec.ErrorDecoder;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class FeignConfigTest {

    private final FeignConfig feignConfig = new FeignConfig();

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(feignConfig, "hubApiUrl", "http://localhost:8081");
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    // --- JWT Relay Interceptor ---

    @Test
    @DisplayName("Deve adicionar header Authorization quando token Bearer estiver presente")
    void deveAdicionarAuthorizationQuandoTokenPresente() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer meu-token-jwt");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        RequestTemplate template = new RequestTemplate();
        feignConfig.jwtRelayInterceptor().apply(template);

        Collection<String> authHeaders = template.headers().get("Authorization");
        assertNotNull(authHeaders);
        assertEquals("Bearer meu-token-jwt", authHeaders.iterator().next());
    }

    @Test
    @DisplayName("Não deve adicionar header Authorization quando não há contexto de requisição")
    void naoDeveAdicionarAuthorizationSemContexto() {
        RequestTemplate template = new RequestTemplate();
        feignConfig.jwtRelayInterceptor().apply(template);

        assertTrue(template.headers().isEmpty() ||
                !template.headers().containsKey("Authorization"));
    }

    @Test
    @DisplayName("Não deve adicionar header Authorization quando token não começa com Bearer")
    void naoDeveAdicionarQuandoTokenNaoEBearer() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        RequestTemplate template = new RequestTemplate();
        feignConfig.jwtRelayInterceptor().apply(template);

        Collection<String> authHeaders = template.headers().get("Authorization");
        assertTrue(authHeaders == null || authHeaders.isEmpty());
    }

    // --- ErrorDecoder ---

    @Test
    @DisplayName("Deve retornar EntityNotFoundException para status 404")
    void deveRetornarEntityNotFoundParaStatus404() {
        ErrorDecoder decoder = feignConfig.hubErrorDecoder();

        Exception ex = decoder.decode("HubRoomClient#findById", buildResponse(404));

        assertInstanceOf(EntityNotFoundException.class, ex);
        assertTrue(ex.getMessage().contains("HubRoomClient#findById"));
    }

    @Test
    @DisplayName("Deve retornar AccessDeniedException para status 401")
    void deveRetornarAccessDeniedParaStatus401() {
        ErrorDecoder decoder = feignConfig.hubErrorDecoder();

        Exception ex = decoder.decode("HubRoomClient#findById", buildResponse(401));

        assertInstanceOf(AccessDeniedException.class, ex);
    }

    @Test
    @DisplayName("Deve retornar AccessDeniedException para status 403")
    void deveRetornarAccessDeniedParaStatus403() {
        ErrorDecoder decoder = feignConfig.hubErrorDecoder();

        Exception ex = decoder.decode("HubUserClient#findById", buildResponse(403));

        assertInstanceOf(AccessDeniedException.class, ex);
    }

    @Test
    @DisplayName("Deve retornar HubIntegrationException para status 500")
    void deveRetornarHubIntegrationExceptionParaStatus500() {
        ErrorDecoder decoder = feignConfig.hubErrorDecoder();

        Exception ex = decoder.decode("HubRoomClient#findById", buildResponse(500));

        assertInstanceOf(HubIntegrationException.class, ex);
        assertTrue(ex.getMessage().contains("500"));
    }

    @Test
    @DisplayName("Deve retornar HubIntegrationException para status 503")
    void deveRetornarHubIntegrationExceptionParaStatus503() {
        ErrorDecoder decoder = feignConfig.hubErrorDecoder();

        Exception ex = decoder.decode("HubUserClient#findById", buildResponse(503));

        assertInstanceOf(HubIntegrationException.class, ex);
        assertTrue(ex.getMessage().contains("503"));
    }

    private Response buildResponse(int status) {
        Request dummyRequest = Request.create(
                Request.HttpMethod.GET,
                "http://localhost:8081/api/test",
                Collections.emptyMap(),
                null,
                null,
                null
        );
        return Response.builder()
                .status(status)
                .reason("reason")
                .request(dummyRequest)
                .headers(Collections.emptyMap())
                .build();
    }
}
