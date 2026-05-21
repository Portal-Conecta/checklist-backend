package com.portal.conecta.checklist.shared.security;

import com.portal.conecta.checklist.module.checklist.presentation.DummyChecklistController;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DummyChecklistController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class, AuthorizationService.class})
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${jwt.secret:default-secret-key-that-is-at-least-256-bits-long-for-hmac-sha-256-portal-conecta-checklist-api}")
    private String jwtSecret;

    private String createToken(String userId, String username, List<String> roles, Long turmaId, List<Long> linkedTurmas, String scope) {
        var key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(userId)
                .claim("name", username)
                .claim("roles", roles)
                .claim("turma_id", turmaId)
                .claim("turmas", linkedTurmas)
                .claim("scope", scope)
                .signWith(key)
                .compact();
    }

    @Test
    @DisplayName("should return 401 when calling endpoint without token")
    void shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/checklists/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", is("Acesso não autorizado: token inválido ou ausente.")))
                .andExpect(jsonPath("$.localDateTime", notNullValue()));
    }

    @Test
    @DisplayName("should return 401 when calling endpoint with invalid token")
    void shouldReturn401WithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/checklists/dashboard")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    @DisplayName("should allow dashboard access for PERFIL_SENAI and block it for APRENDIZ")
    void dashboardAccessControl() throws Exception {
        // APRENDIZ -> 403 Forbidden
        String aprendizToken = createToken("1", "AUser", List.of("APRENDIZ"), null, List.of(), null);
        mockMvc.perform(get("/api/checklists/dashboard")
                        .header("Authorization", "Bearer " + aprendizToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.message", is("Acesso negado: você não tem permissão para acessar esta funcionalidade.")));

        // PERFIL_SENAI -> 200 OK
        String senaiToken = createToken("2", "SUser", List.of("PERFIL_SENAI"), null, List.of(), null);
        mockMvc.perform(get("/api/checklists/dashboard")
                        .header("Authorization", "Bearer " + senaiToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is("Dashboard data loaded successfully.")));
    }

    @Test
    @DisplayName("should allow REPRESENTANTE to create execution for their own class, deny for others")
    void representanteExecutionCreation() throws Exception {
        String token = createToken("3", "RUser", List.of("REPRESENTANTE"), 100L, List.of(), null);

        // Own class -> 200 OK
        mockMvc.perform(post("/api/checklists/executions")
                        .header("Authorization", "Bearer " + token)
                        .param("turmaId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is("Execution created successfully for class: 100")));

        // Other class -> 403 Forbidden
        mockMvc.perform(post("/api/checklists/executions")
                        .header("Authorization", "Bearer " + token)
                        .param("turmaId", "101"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("should allow DOCENTE to create execution for linked classes, deny for others")
    void docenteExecutionCreation() throws Exception {
        String token = createToken("4", "DUser", List.of("DOCENTE"), null, List.of(200L, 201L), null);

        // Linked class -> 200 OK
        mockMvc.perform(post("/api/checklists/executions")
                        .header("Authorization", "Bearer " + token)
                        .param("turmaId", "200"))
                .andExpect(status().isOk());

        // Unlinked class -> 403 Forbidden
        mockMvc.perform(post("/api/checklists/executions")
                        .header("Authorization", "Bearer " + token)
                        .param("turmaId", "202"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should allow editing completed checklists matching organization scope")
    void completedChecklistEditScopeControl() throws Exception {
        // SENAI profile editing SENAI -> 200 OK
        String senaiToken = createToken("5", "SUser", List.of("PERFIL_SENAI"), null, List.of(), "SENAI");
        mockMvc.perform(put("/api/checklists/completed")
                        .header("Authorization", "Bearer " + senaiToken)
                        .param("scope", "SENAI"))
                .andExpect(status().isOk());

        // SENAI profile editing WEG -> 403 Forbidden
        mockMvc.perform(put("/api/checklists/completed")
                        .header("Authorization", "Bearer " + senaiToken)
                        .param("scope", "WEG"))
                .andExpect(status().isForbidden());

        // WEG profile editing WEG -> 200 OK
        String wegToken = createToken("6", "WUser", List.of("PERFIL_WEG"), null, List.of(), "WEG");
        mockMvc.perform(put("/api/checklists/completed")
                        .header("Authorization", "Bearer " + wegToken)
                        .param("scope", "WEG"))
                .andExpect(status().isOk());
    }
}
