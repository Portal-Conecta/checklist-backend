package com.portal.conecta.checklist.shared.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.shared.integration.hub.exception.HubIntegrationException;
import com.portal.conecta.checklist.shared.security.error.SecurityErrorResponseWriter;
import com.portal.conecta.checklist.shared.security.token.HubJwtTokenProvider;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HubJwtAuthenticationFilterTest {

    private final HubJwtTokenProvider tokenProvider = mock(HubJwtTokenProvider.class);
    private final HubJwtAuthenticationFilter filter = new HubJwtAuthenticationFilter(
            tokenProvider,
            new SecurityErrorResponseWriter(new ObjectMapper().findAndRegisterModules())
    );

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateWhenBearerTokenIsValid() throws ServletException, IOException {
        String token = "valid-token";
        var authentication = new UsernamePasswordAuthenticationToken("user-id", token, List.of());
        MockHttpServletRequest request = requestWithBearerToken(token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(tokenProvider.getAuthentication(token)).thenReturn(authentication);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(authentication);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getDetails()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
        verify(tokenProvider).getAuthentication(token);
    }

    @Test
    void shouldRejectMalformedAuthorizationHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("\"status\":401");
        assertThat(response.getContentAsString()).contains("\"message\":\"Token ausente ou formatado incorretamente.\"");
    }

    @Test
    void shouldRejectInvalidBearerToken() throws ServletException, IOException {
        String token = "invalid-token";
        MockHttpServletRequest request = requestWithBearerToken(token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(tokenProvider.getAuthentication(token))
                .thenThrow(new BadCredentialsException("Token do Hub invalido ou expirado."));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("\"status\":401");
        assertThat(response.getContentAsString()).contains("\"message\":\"Token do Hub invalido ou expirado.\"");
        verify(tokenProvider).getAuthentication(token);
    }

    @Test
    void shouldReturnServiceUnavailableWhenHubValidationFails() throws ServletException, IOException {
        String token = "valid-token-with-hub-down";
        MockHttpServletRequest request = requestWithBearerToken(token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(tokenProvider.getAuthentication(token))
                .thenThrow(new HubIntegrationException("Servico de usuarios do Hub indisponivel."));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.getContentAsString()).contains("\"status\":503");
        assertThat(response.getContentAsString()).contains("\"message\":\"Servico de usuarios do Hub indisponivel.\"");
        verify(tokenProvider).getAuthentication(token);
    }

    private MockHttpServletRequest requestWithBearerToken(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }
}
