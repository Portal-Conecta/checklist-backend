package com.portal.conecta.checklist.shared.security.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.shared.exception.ApiError;
import com.portal.conecta.checklist.shared.security.config.SecurityConfig;
import com.portal.conecta.checklist.shared.security.filter.HubJwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Componente utilitário que serializa erros de segurança (autenticação e autorização)
 * como JSON no formato {@link ApiError} diretamente na resposta HTTP.
 *
 * <p>Utilizado pelo {@link HubJwtAuthenticationFilter} e pelos handlers de exceção de
 * segurança configurados no {@link SecurityConfig}. Garante que erros de segurança
 * retornem o mesmo formato padrão de erro que o restante da API.</p>
 *
 * <p>Ignorado silenciosamente se a resposta já tiver sido committed.</p>
 */


@Component
public class SecurityErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public SecurityErrorResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Escreve um erro de segurança como JSON na resposta HTTP.
     *
     * @param request  requisição HTTP original (não utilizada, disponível para extensão)
     * @param response resposta HTTP a ser preenchida
     * @param status   código de status HTTP do erro (ex: {@code 401}, {@code 403}, {@code 503})
     * @param message  mensagem descritiva do erro para o cliente
     * @throws IOException se ocorrer falha ao escrever na resposta
     */

    public void write(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String message
    ) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(
                response.getWriter(),
                new ApiError(
                        Instant.now().toString(),
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        request.getRequestURI()
                )
        );
    }
}
