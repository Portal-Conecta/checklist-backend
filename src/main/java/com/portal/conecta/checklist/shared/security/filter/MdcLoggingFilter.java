package com.portal.conecta.checklist.shared.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.portal.conecta.checklist.shared.context.RequestContext;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Filtro que popula o {@link MDC} com o contexto da requisicao para logging estruturado.
 *
 * <p>Segue o contrato de campos do padrao Hub ({@code portal-logging}): {@code correlationId},
 * {@code method}, {@code path}, {@code service}, {@code userId}, {@code userType},
 * {@code status} e {@code durationMs}.</p>
 *
 * <p>Executa em {@link Ordered#HIGHEST_PRECEDENCE} para envolver toda a cadeia de filtros.
 * O {@code userId}/{@code userType} so ficam disponiveis apos o filtro de autenticacao JWT,
 * por isso sao lidos apos {@link FilterChain#doFilter}.</p>
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            String correlationId = Optional.ofNullable(request.getHeader("X-Correlation-Id"))
                    .filter(s -> !s.isBlank())
                    .orElse(UUID.randomUUID().toString());

            MDC.put("correlationId", correlationId);
            MDC.put("method", request.getMethod());
            MDC.put("path", request.getRequestURI());
            MDC.put("service", "conecta.checklist");

            filterChain.doFilter(request, response);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof RequestContext ctx) {
                MDC.put("userId", ctx.userId().toString());
                MDC.put("userType", ctx.userType().name());
            }
        } finally {
            MDC.put("status", String.valueOf(response.getStatus()));
            MDC.put("durationMs", String.valueOf(System.currentTimeMillis() - start));
            log.info("Request concluido");
            MDC.clear();
        }
    }
}
