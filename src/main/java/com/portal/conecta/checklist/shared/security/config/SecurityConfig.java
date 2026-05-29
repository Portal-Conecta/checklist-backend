package com.portal.conecta.checklist.shared.security.config;

import com.portal.conecta.checklist.shared.hub.properties.HubApiProperties;
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
                    authorize.requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/info").permitAll();

                    if (swaggerPublic) {
                        authorize.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**").permitAll();
                    }

                    authorize.anyRequest().authenticated();
                })
                .addFilterBefore(hubJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
