package com.portal.conecta.checklist.shared.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração para validar a configuração de CORS no {@link SecurityConfig}.
 *
 * <p>Utiliza {@link MockMvc} com o contexto completo do Spring Boot para garantir que
 * os headers CORS são retornados corretamente nas respostas de preflight (OPTIONS).</p>
 */
@SpringBootTest(properties = {
        "checklist.security.jwt.secret=dGVzdC1vbmx5LWp3dC1zZWNyZXQtMzItYnl0ZXMtbm90LXJlYWw=",
        "hub.api.url=http://localhost:8080",
        "checklist.cors.allowed-origins=http://localhost:3000,http://localhost:5173"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CorsConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void preflightDeveRetornarHeadersCorretos() throws Exception {
        mockMvc.perform(options("/api/checklist-templates")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().exists("Access-Control-Allow-Methods"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void preflightDeveRetornarHeadersCorretosParaSegundaOrigem() throws Exception {
        mockMvc.perform(options("/api/checklist-templates")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().exists("Access-Control-Allow-Methods"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void preflightDeveRejeitarOrigemNaoPermitida() throws Exception {
        mockMvc.perform(options("/api/checklist-templates")
                        .header("Origin", "http://malicious-site.com")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void preflightNaoDeveAplicarCorsForaDoPrefixoApi() throws Exception {
        mockMvc.perform(options("/actuator/health")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
