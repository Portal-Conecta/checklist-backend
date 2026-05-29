package com.portal.conecta.checklist.shared.security.filter;

import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.hub.exception.HubIntegrationException;
import com.portal.conecta.checklist.shared.security.config.SecurityConfig;
import com.portal.conecta.checklist.shared.security.error.SecurityErrorResponseWriter;
import com.portal.conecta.checklist.shared.security.token.HubJwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticação que intercepta todas as requisições e valida o JWT emitido pelo Hub.
 *
 * <p>Fluxo de execução por requisição:</p>
 * <ol>
 *   <li>Se não houver cabeçalho {@code Authorization}, passa para o próximo filtro
 *       (o Spring Security rejeitará rotas protegidas sem autenticação).</li>
 *   <li>Se o cabeçalho não iniciar com {@code "Bearer "}, rejeita com {@code 401}.</li>
 *   <li>Delega a validação e extração do contexto para {@link HubJwtTokenProvider}.</li>
 *   <li>Em caso de token inválido ou expirado, rejeita com {@code 401}.</li>
 *   <li>Em caso de Hub indisponível durante a validação, rejeita com {@code 503}.</li>
 *   <li>Em sucesso, armazena o {@link RequestContext} no {@link SecurityContextHolder}.</li>
 * </ol>
 *
 * <p>Registrado antes de {@link UsernamePasswordAuthenticationFilter} na cadeia de filtros.
 * O bean do filtro é desabilitado no registro automático do servlet para evitar
 * dupla execução — é gerenciado exclusivamente pelo Spring Security.</p>
 *
 * @see HubJwtTokenProvider
 * @see SecurityConfig
 */

@Component
@RequiredArgsConstructor
public class HubJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final HubJwtTokenProvider tokenProvider;
    private final SecurityErrorResponseWriter securityErrorResponseWriter;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            reject(request, response, HttpStatus.UNAUTHORIZED, "Token ausente ou formatado incorretamente.");
            return;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length());

        try {
            Authentication authentication = tokenProvider.getAuthentication(token);
            if (authentication instanceof AbstractAuthenticationToken authenticationToken) {
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            }
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (BadCredentialsException exception) {
            reject(request, response, HttpStatus.UNAUTHORIZED, "Token do Hub invalido ou expirado.");
            return;
        } catch (HubIntegrationException exception) {
            reject(request, response, HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void reject(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String message
    ) throws IOException {
        SecurityContextHolder.clearContext();
        securityErrorResponseWriter.write(request, response, status, message);
    }
}
