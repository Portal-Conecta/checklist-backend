package com.portal.conecta.checklist.shared.security.config;

import com.portal.conecta.checklist.shared.integration.hub.config.HubApiProperties;
import com.portal.conecta.checklist.shared.security.error.SecurityErrorResponseWriter;
import com.portal.conecta.checklist.shared.security.filter.HubJwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuração central de segurança da aplicação.
 *
 * <p>Define:</p>
 * <ul>
 *   <li>Política stateless (sem sessão HTTP).</li>
 *   <li>CSRF, form login, HTTP basic e logout desabilitados.</li>
 *   <li>Rotas públicas: {@code GET /actuator/health} (incluindo os probes
 *       {@code /actuator/health/**}), {@code GET /actuator/info} e
 *       {@code GET /actuator/prometheus}. O endpoint de métricas é exposto sem
 *       autenticação para o scraper (Alloy/Prometheus) na rede interna, seguindo
 *       o mesmo contrato dos demais serviços do portal.
 *       Swagger liberado condicionalmente via {@code checklist.security.swagger-public=true}.</li>
 *   <li>As demais rotas exigem autenticação via JWT.</li>
 *   <li>{@link HubJwtAuthenticationFilter} inserido antes do filtro padrão do Spring Security.</li>
 *   <li>Respostas padronizadas para {@code 401 Unauthorized} e {@code 403 Forbidden}
 *       via {@link SecurityErrorResponseWriter}.</li>
 * </ul>
 *
 * <p>O bean do {@link HubJwtAuthenticationFilter} é desregistrado do servlet container
 * para evitar dupla execução — é gerenciado exclusivamente pelo Spring Security.</p>
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({
        HubJwtProperties.class,
        HubApiProperties.class
})
public class SecurityConfig {

    private final HubJwtAuthenticationFilter hubJwtAuthenticationFilter;
    private final SecurityErrorResponseWriter securityErrorResponseWriter;

    @Value("${checklist.security.swagger-public:false}")
    private boolean swaggerPublic;

    @Bean
    public FilterRegistrationBean<HubJwtAuthenticationFilter> hubJwtAuthenticationFilterRegistration() {
        FilterRegistrationBean<HubJwtAuthenticationFilter> registration = new FilterRegistrationBean<>(hubJwtAuthenticationFilter);
        registration.setEnabled(false);
        return registration;
    }

    private static final String[] SWAGGER_PATHS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**"
    };

    private static final String[] PUBLIC_ACTUATOR_PATHS = {
            "/actuator/health",
            "/actuator/health/**",
            "/actuator/info",
            "/actuator/prometheus"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                securityErrorResponseWriter.write(
                                        request,
                                        response,
                                        HttpStatus.UNAUTHORIZED,
                                        "Token de autenticacao e obrigatorio."
                                ))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                securityErrorResponseWriter.write(
                                        request,
                                        response,
                                        HttpStatus.FORBIDDEN,
                                        "Acesso negado."
                                ))
                )
                .authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers(HttpMethod.GET, PUBLIC_ACTUATOR_PATHS).permitAll();

                    if (swaggerPublic) {
                        authorize.requestMatchers(SWAGGER_PATHS).permitAll();
                    } else {
                        authorize.requestMatchers(SWAGGER_PATHS).denyAll();
                    }

                    authorize.anyRequest().authenticated();
                })
                .addFilterBefore(hubJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
