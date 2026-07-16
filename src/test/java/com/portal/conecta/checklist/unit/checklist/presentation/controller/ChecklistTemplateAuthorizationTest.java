package com.portal.conecta.checklist.modules.checklist.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.modules.checklist.application.port.out.integration.HubRoomProvider;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.modules.checklist.domain.enums.AnswerType;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistItem;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSchema;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSection;
import com.portal.conecta.checklist.shared.context.ClassRole;
import com.portal.conecta.checklist.shared.context.ContextClass;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.TypeUser;
import com.portal.conecta.checklist.modules.checklist.infrastructure.persistence.ChecklistTemplateRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de autorizacao end-to-end (real SecurityConfig + use cases reais) para
 * ChecklistTemplateController. Apenas os ports de saida (repositorio + Hub) sao
 * mockados; a logica de permissao real (RequestContext.canX + AccessDeniedException
 * dentro dos use cases) e exercida de verdade — se alguem remover uma checagem de
 * RequestContext, estes testes falham.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = {
        "checklist.security.jwt.secret=dGVzdC1vbmx5LWp3dC1zZWNyZXQtMzItYnl0ZXMtbm90LXJlYWw=",
        "hub.api.url=http://localhost:8080",
        // Todos os ports de persistencia sao mockados neste teste (@MockitoBean) — nao ha
        // necessidade de um Postgres real via Testcontainers. Desligamos Flyway (que so
        // roda migrations Postgres-specificas) e forcamos o dialeto H2 para permitir que o
        // EntityManagerFactory suba contra o H2 em memoria sem tentar falar com um Postgres.
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.generate-ddl=false",
        "spring.jpa.properties.hibernate.hbm2ddl.auto=none",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@ActiveProfiles("test")
class ChecklistTemplateAuthorizationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChecklistTemplateRepository templateRepository;

    @MockitoBean
    private HubRoomProvider hubRoomProvider;

    private MockMvc mockMvc;

    private MockMvc mockMvc() {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                    .apply(SecurityMockMvcConfigurers.springSecurity())
                    .build();
        }
        return mockMvc;
    }

    // ---- perform helpers -------------------------------------------------

    private MockHttpServletRequestBuilder authed(MockHttpServletRequestBuilder builder, RequestContext user) {
        return builder.with(authentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of())
        ));
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

    private RequestContext representativeWithClass(UUID classId) {
        return new RequestContext(UUID.randomUUID(), TypeUser.REPRESENTATIVE, List.of(new ContextClass(classId, ClassRole.REPRESENTATIVE)));
    }

    private RequestContext student() {
        return new RequestContext(UUID.randomUUID(), TypeUser.STUDENT);
    }

    private RequestContext admin() {
        return new RequestContext(UUID.randomUUID(), TypeUser.ADMIN);
    }

    private String validCreateBody() throws Exception {
        var schema = new ChecklistSchema(List.of(new ChecklistSection(
                "estrutura", "Estrutura", 1,
                List.of(new ChecklistItem("quadro", "Quadro em bom estado?", "Verificar quadro", AnswerType.CONFORMITY, true, 1))
        )));
        String body = """
                {"roomId":"%s","title":"Checklist padrao","description":"desc","category":"GERAL","schemaJson":%s}
                """.formatted(UUID.randomUUID(), objectMapper.writeValueAsString(schema));
        return body;
    }

    private ChecklistTemplate draftTemplate(UUID id, UUID roomId) {
        return ChecklistTemplate.builder()
                .id(id).roomId(roomId).title("t").description("d").version(1)
                .status(ChecklistTemplateStatus.DRAFT).active(false)
                .templateGroupId(UUID.randomUUID())
                .build();
    }

    private ChecklistTemplate activeTemplate(UUID id, UUID roomId) {
        return ChecklistTemplate.builder()
                .id(id).roomId(roomId).title("t").description("d").version(1)
                .status(ChecklistTemplateStatus.ACTIVE).active(true)
                .templateGroupId(UUID.randomUUID())
                .build();
    }

    // ---- 401 sem token -----------------------------------------------------

    @Test
    void semTokenRetorna401NoCreate() throws Exception {
        mockMvc().perform(post("/api/checklist-templates").contentType(APPLICATION_JSON).content(validCreateBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void semTokenRetorna401NoActivate() throws Exception {
        mockMvc().perform(patch("/api/checklist-templates/" + UUID.randomUUID() + "/activate"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void semTokenRetorna401NoList() throws Exception {
        mockMvc().perform(get("/api/checklist-templates")).andExpect(status().isUnauthorized());
    }

    @Test
    void semTokenRetorna401NoFindById() throws Exception {
        mockMvc().perform(get("/api/checklist-templates/" + UUID.randomUUID())).andExpect(status().isUnauthorized());
    }

    @Test
    void semTokenRetorna401NoEdit() throws Exception {
        mockMvc().perform(patch("/api/checklist-templates/" + UUID.randomUUID())
                        .contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void semTokenRetorna401NoNewVersion() throws Exception {
        mockMvc().perform(post("/api/checklist-templates/" + UUID.randomUUID() + "/new-version"))
                .andExpect(status().isUnauthorized());
    }

    // ---- criar template ------------------------------------------------

    @Test
    void senaiCriaTemplateComSucesso() throws Exception {
        when(hubRoomProvider.existsById(any())).thenReturn(true);
        when(templateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc().perform(authed(post("/api/checklist-templates"), senai())
                        .contentType(APPLICATION_JSON).content(validCreateBody()))
                .andExpect(status().isCreated());
    }

    @Test
    void wegCriaTemplateComSucesso() throws Exception {
        when(hubRoomProvider.existsById(any())).thenReturn(true);
        when(templateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc().perform(authed(post("/api/checklist-templates"), weg())
                        .contentType(APPLICATION_JSON).content(validCreateBody()))
                .andExpect(status().isCreated());
    }

    @Test
    void teacherNaoCriaTemplate() throws Exception {
        UUID classId = UUID.randomUUID();
        mockMvc().perform(authed(post("/api/checklist-templates"), teacherWithClass(classId))
                        .contentType(APPLICATION_JSON).content(validCreateBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void representativeNaoCriaTemplate() throws Exception {
        UUID classId = UUID.randomUUID();
        mockMvc().perform(authed(post("/api/checklist-templates"), representativeWithClass(classId))
                        .contentType(APPLICATION_JSON).content(validCreateBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentNaoCriaTemplate() throws Exception {
        mockMvc().perform(authed(post("/api/checklist-templates"), student())
                        .contentType(APPLICATION_JSON).content(validCreateBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminNaoCriaTemplate() throws Exception {
        mockMvc().perform(authed(post("/api/checklist-templates"), admin())
                        .contentType(APPLICATION_JSON).content(validCreateBody()))
                .andExpect(status().isForbidden());
    }

    // ---- ativar template ------------------------------------------------

    @Test
    void senaiAtivaTemplateComSucesso() throws Exception {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        ChecklistTemplate template = draftTemplate(templateId, roomId);
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(templateRepository.findByTemplateGroupIdAndStatus(any(), any())).thenReturn(List.of());
        when(hubRoomProvider.findById(roomId)).thenReturn(Optional.of(new com.portal.conecta.checklist.modules.checklist.domain.valueobject.RoomReference(roomId)));
        when(templateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc().perform(authed(patch("/api/checklist-templates/" + templateId + "/activate"), senai()))
                .andExpect(status().isOk());
    }

    @Test
    void teacherNaoAtivaTemplate() throws Exception {
        mockMvc().perform(authed(patch("/api/checklist-templates/" + UUID.randomUUID() + "/activate"), teacherWithClass(UUID.randomUUID())))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentNaoAtivaTemplate() throws Exception {
        mockMvc().perform(authed(patch("/api/checklist-templates/" + UUID.randomUUID() + "/activate"), student()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminNaoAtivaTemplate() throws Exception {
        mockMvc().perform(authed(patch("/api/checklist-templates/" + UUID.randomUUID() + "/activate"), admin()))
                .andExpect(status().isForbidden());
    }

    // ---- listar templates -------------------------------------------------

    @Test
    void senaiListaTodosOsTemplates() throws Exception {
        when(templateRepository.findAll()).thenReturn(List.of());
        mockMvc().perform(authed(get("/api/checklist-templates"), senai())).andExpect(status().isOk());
    }

    @Test
    void teacherComTurmaListaTemplatesAtivos() throws Exception {
        when(templateRepository.findAllByActiveTrueAndStatus(ChecklistTemplateStatus.ACTIVE)).thenReturn(List.of());
        mockMvc().perform(authed(get("/api/checklist-templates"), teacherWithClass(UUID.randomUUID())))
                .andExpect(status().isOk());
    }

    @Test
    void representativeComTurmaListaTemplatesAtivos() throws Exception {
        when(templateRepository.findAllByActiveTrueAndStatus(ChecklistTemplateStatus.ACTIVE)).thenReturn(List.of());
        mockMvc().perform(authed(get("/api/checklist-templates"), representativeWithClass(UUID.randomUUID())))
                .andExpect(status().isOk());
    }

    @Test
    void studentSemTurmaNaoListaTemplates() throws Exception {
        // STUDENT sem nenhum vinculo de turma falha canAccessChecklistModule() -> 403.
        mockMvc().perform(authed(get("/api/checklist-templates"), student())).andExpect(status().isForbidden());
    }

    @Test
    void adminNaoListaTemplates() throws Exception {
        mockMvc().perform(authed(get("/api/checklist-templates"), admin())).andExpect(status().isForbidden());
    }

    // ---- buscar template por id (draft vs active) --------------------------

    @Test
    void teacherNaoVeTemplateDraft() throws Exception {
        UUID templateId = UUID.randomUUID();
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(draftTemplate(templateId, UUID.randomUUID())));

        mockMvc().perform(authed(get("/api/checklist-templates/" + templateId), teacherWithClass(UUID.randomUUID())))
                .andExpect(status().isForbidden());
    }

    @Test
    void teacherVeTemplateAtivo() throws Exception {
        UUID templateId = UUID.randomUUID();
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(activeTemplate(templateId, UUID.randomUUID())));

        mockMvc().perform(authed(get("/api/checklist-templates/" + templateId), teacherWithClass(UUID.randomUUID())))
                .andExpect(status().isOk());
    }

    @Test
    void senaiVeTemplateDraft() throws Exception {
        UUID templateId = UUID.randomUUID();
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(draftTemplate(templateId, UUID.randomUUID())));

        mockMvc().perform(authed(get("/api/checklist-templates/" + templateId), senai()))
                .andExpect(status().isOk());
    }

    @Test
    void studentNaoVeTemplatePorId() throws Exception {
        mockMvc().perform(authed(get("/api/checklist-templates/" + UUID.randomUUID()), student()))
                .andExpect(status().isForbidden());
    }

    // ---- editar template (gerencial) --------------------------------------

    @Test
    void teacherNaoEditaTemplate() throws Exception {
        mockMvc().perform(authed(patch("/api/checklist-templates/" + UUID.randomUUID()), teacherWithClass(UUID.randomUUID()))
                        .contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentNaoEditaTemplate() throws Exception {
        mockMvc().perform(authed(patch("/api/checklist-templates/" + UUID.randomUUID()), student())
                        .contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    // ---- nova versao (gerencial) -------------------------------------------

    @Test
    void teacherNaoCriaNovaVersao() throws Exception {
        mockMvc().perform(authed(post("/api/checklist-templates/" + UUID.randomUUID() + "/new-version"), teacherWithClass(UUID.randomUUID())))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentNaoCriaNovaVersao() throws Exception {
        mockMvc().perform(authed(post("/api/checklist-templates/" + UUID.randomUUID() + "/new-version"), student()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminNaoCriaNovaVersao() throws Exception {
        mockMvc().perform(authed(post("/api/checklist-templates/" + UUID.randomUUID() + "/new-version"), admin()))
                .andExpect(status().isForbidden());
    }

    // NOTA: GET /api/checklist-templates/items/search (SearchChecklistItemUseCase)
    // nao esta coberto aqui porque, ao contrario de todos os outros use cases deste
    // controller, ele lanca IllegalArgumentException (-> 400) em vez de
    // AccessDeniedException (-> 403) quando o usuario nao pode acessar o modulo.
    // Isso e uma inconsistencia no codigo de producao, nao um erro deste teste.
}
