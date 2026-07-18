package com.portal.conecta.checklist.module.checklist.presentation.controller;

import com.portal.conecta.checklist.module.checklist.application.port.out.integration.HubClassProvider;
import com.portal.conecta.checklist.module.checklist.application.port.out.persistence.ChecklistSubmissionWindowRepositoryPort;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.module.checklist.domain.enums.Shift;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistSubmissionWindow;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.ClassReference;
import com.portal.conecta.checklist.shared.context.ClassRole;
import com.portal.conecta.checklist.shared.context.ContextClass;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.TypeUser;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistTemplateRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de autorizacao end-to-end para SubmissionWindowController, exercitando
 * o SecurityConfig real e os use cases reais (apenas os ports de saida sao
 * mockados).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = {
        "checklist.security.jwt.secret=dGVzdC1vbmx5LWp3dC1zZWNyZXQtMzItYnl0ZXMtbm90LXJlYWw=",
        "hub.api.url=http://localhost:8080",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.generate-ddl=false",
        "spring.jpa.properties.hibernate.hbm2ddl.auto=none",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@ActiveProfiles("test")
class SubmissionWindowAuthorizationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private ChecklistSubmissionWindowRepositoryPort repository;

    @MockitoBean
    private HubClassProvider hubClassProvider;

    // ChecklistTemplateRepository (tipo concreto) precisa ser mockado a parte —
    // ChecklistItemQueryRepository (usado por SearchChecklistItemUseCase, que faz parte
    // do ChecklistTemplateController tambem carregado neste contexto Spring completo)
    // depende do tipo concreto diretamente, nao do ChecklistTemplateRepositoryPort.
    @MockitoBean
    private ChecklistTemplateRepository templateRepository;

    private MockMvc mockMvc;

    private MockMvc mockMvc() {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                    .apply(SecurityMockMvcConfigurers.springSecurity())
                    .build();
        }
        return mockMvc;
    }

    private MockHttpServletRequestBuilder authed(MockHttpServletRequestBuilder builder, RequestContext user) {
        return builder.with(authentication(new UsernamePasswordAuthenticationToken(user, null, List.of())));
    }

    private RequestContext senai() {
        return new RequestContext(UUID.randomUUID(), TypeUser.SENAI);
    }

    private RequestContext weg() {
        return new RequestContext(UUID.randomUUID(), TypeUser.WEG);
    }

    private RequestContext teacherWithClass(UUID classId) {
        return new RequestContext(UUID.randomUUID(), TypeUser.TEACHER, List.of(new ContextClass(classId, ClassRole.TEACHER)));
    }

    private RequestContext student() {
        return new RequestContext(UUID.randomUUID(), TypeUser.STUDENT);
    }

    private RequestContext admin() {
        return new RequestContext(UUID.randomUUID(), TypeUser.ADMIN);
    }

    private String upsertBody() {
        return """
                {"openAt":"08:00","durationMinutes":30}
                """;
    }

    // ---- 401 sem token ------------------------------------------------------

    @Test
    void semTokenRetorna401NoListAll() throws Exception {
        mockMvc().perform(get("/api/submission-windows")).andExpect(status().isUnauthorized());
    }

    @Test
    void semTokenRetorna401NoListByClass() throws Exception {
        mockMvc().perform(get("/api/submission-windows/classes/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void semTokenRetorna401NoUpsert() throws Exception {
        mockMvc().perform(put("/api/submission-windows/classes/" + UUID.randomUUID() + "/ARRIVAL")
                        .contentType(APPLICATION_JSON).content(upsertBody()))
                .andExpect(status().isUnauthorized());
    }

    // ---- listar (global) — restrito a SENAI/WEG na implementacao atual --------
    // REVISAR COM O TIME (ADR-005): a matriz do ADR-005 descreve "Listar janelas"
    // como liberado para "Qualquer autenticado", mas ListSubmissionWindowsUseCase.execute()
    // (GET /api/submission-windows, listagem global) hoje exige canManageChecklistTemplates()
    // (SENAI/WEG). Os testes abaixo documentam o comportamento REAL do codigo atual.

    @Test
    void senaiListaJanelasGlobalmente() throws Exception {
        when(repository.findAllByOrderByClassIdAscChecklistTypeAsc()).thenReturn(List.of());
        mockMvc().perform(authed(get("/api/submission-windows"), senai())).andExpect(status().isOk());
    }

    @Test
    void wegListaJanelasGlobalmente() throws Exception {
        when(repository.findAllByOrderByClassIdAscChecklistTypeAsc()).thenReturn(List.of());
        mockMvc().perform(authed(get("/api/submission-windows"), weg())).andExpect(status().isOk());
    }

    @Test
    void teacherNaoListaJanelasGlobalmente() throws Exception {
        mockMvc().perform(authed(get("/api/submission-windows"), teacherWithClass(UUID.randomUUID())))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentNaoListaJanelasGlobalmente() throws Exception {
        mockMvc().perform(authed(get("/api/submission-windows"), student())).andExpect(status().isForbidden());
    }

    @Test
    void adminListaJanelasGlobalmente() throws Exception {
        when(repository.findAllByOrderByClassIdAscChecklistTypeAsc()).thenReturn(List.of());
        mockMvc().perform(authed(get("/api/submission-windows"), admin())).andExpect(status().isOk());
    }

    // ---- listar por turma — SEM checagem de autorizacao na implementacao atual ---
    // REVISAR COM O TIME (ADR-005): ListSubmissionWindowsUseCase.execute(UUID classId)
    // (GET /api/submission-windows/classes/{classId}) nao chama contextProvider nem
    // qualquer RequestContext.canX(...) — QUALQUER usuario autenticado consegue listar
    // as janelas de QUALQUER turma, mesmo sem vinculo com ela. Nao e so uma divergencia
    // da matriz do ADR-005: e uma ausencia total de escopo por turma que vale revisar
    // como possivel gap de seguranca, nao apenas um "qualquer autenticado" intencional.

    @Test
    void qualquerAutenticadoListaJanelasPorTurma() throws Exception {
        UUID classId = UUID.randomUUID();
        when(repository.findAllByClassIdOrderByChecklistTypeAsc(classId)).thenReturn(List.of());

        mockMvc().perform(authed(get("/api/submission-windows/classes/" + classId), student()))
                .andExpect(status().isOk());
        mockMvc().perform(authed(get("/api/submission-windows/classes/" + classId), admin()))
                .andExpect(status().isOk());
    }

    // ---- configurar janela (upsert) — restrito a SENAI/WEG ---------------------

    @Test
    void senaiConfiguraJanelaComSucesso() throws Exception {
        UUID classId = UUID.randomUUID();
        when(hubClassProvider.findById(classId))
                .thenReturn(Optional.of(new ClassReference(classId, "Turma", 1, Shift.FULL_AM_PM, null, null)));
        when(repository.findByClassIdAndChecklistType(classId, ChecklistType.ARRIVAL)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0, ChecklistSubmissionWindow.class));

        mockMvc().perform(authed(put("/api/submission-windows/classes/" + classId + "/ARRIVAL"), senai())
                        .contentType(APPLICATION_JSON).content(upsertBody()))
                .andExpect(status().isOk());
    }

    @Test
    void wegConfiguraJanelaComSucesso() throws Exception {
        UUID classId = UUID.randomUUID();
        when(hubClassProvider.findById(classId))
                .thenReturn(Optional.of(new ClassReference(classId, "Turma", 1, Shift.FULL_AM_PM, null, null)));
        when(repository.findByClassIdAndChecklistType(classId, ChecklistType.ARRIVAL)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0, ChecklistSubmissionWindow.class));

        mockMvc().perform(authed(put("/api/submission-windows/classes/" + classId + "/ARRIVAL"), weg())
                        .contentType(APPLICATION_JSON).content(upsertBody()))
                .andExpect(status().isOk());
    }

    @Test
    void teacherNaoConfiguraJanela() throws Exception {
        UUID classId = UUID.randomUUID();
        mockMvc().perform(authed(put("/api/submission-windows/classes/" + classId + "/ARRIVAL"), teacherWithClass(classId))
                        .contentType(APPLICATION_JSON).content(upsertBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void representativeNaoConfiguraJanela() throws Exception {
        UUID classId = UUID.randomUUID();
        RequestContext representative = new RequestContext(UUID.randomUUID(), TypeUser.REPRESENTATIVE,
                List.of(new ContextClass(classId, ClassRole.REPRESENTATIVE)));

        mockMvc().perform(authed(put("/api/submission-windows/classes/" + classId + "/ARRIVAL"), representative)
                        .contentType(APPLICATION_JSON).content(upsertBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentNaoConfiguraJanela() throws Exception {
        UUID classId = UUID.randomUUID();
        mockMvc().perform(authed(put("/api/submission-windows/classes/" + classId + "/ARRIVAL"), student())
                        .contentType(APPLICATION_JSON).content(upsertBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminConfiguraJanelaComSucesso() throws Exception {
        UUID classId = UUID.randomUUID();
        when(hubClassProvider.findById(classId))
                .thenReturn(Optional.of(new ClassReference(classId, "Turma", 1, Shift.FULL_AM_PM, null, null)));
        when(repository.findByClassIdAndChecklistType(classId, ChecklistType.ARRIVAL)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0, ChecklistSubmissionWindow.class));

        mockMvc().perform(authed(put("/api/submission-windows/classes/" + classId + "/ARRIVAL"), admin())
                        .contentType(APPLICATION_JSON).content(upsertBody()))
                .andExpect(status().isOk());
    }
}
