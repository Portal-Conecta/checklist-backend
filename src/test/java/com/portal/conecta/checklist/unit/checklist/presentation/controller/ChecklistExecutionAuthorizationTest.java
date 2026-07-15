package com.portal.conecta.checklist.modules.checklist.presentation.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.modules.checklist.application.port.out.integration.HubClassProvider;
import com.portal.conecta.checklist.modules.checklist.application.port.out.integration.HubCourseProvider;
import com.portal.conecta.checklist.modules.checklist.application.port.out.integration.HubRoomProvider;
import com.portal.conecta.checklist.modules.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistSubmissionWindowRepositoryPort;
import com.portal.conecta.checklist.modules.checklist.infrastructure.persistence.ChecklistTemplateRepository;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.modules.checklist.domain.enums.Shift;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.modules.checklist.domain.enums.AnswerType;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistItem;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSchema;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSection;
import com.portal.conecta.checklist.modules.checklist.domain.valueobject.ClassReference;
import com.portal.conecta.checklist.shared.context.ClassRole;
import com.portal.conecta.checklist.shared.context.ContextClass;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.TypeUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de autorizacao end-to-end para ChecklistExecutionController. Apenas os
 * ports de saida (repositorios + Hub) sao mockados; os servicos internos
 * (ChecklistExecutionDataMapper, ChecklistExecutionScoringService, ChecklistIssueService,
 * ChecklistExecutionAnswerValidationService, SubmissionWindowValidator) sao os
 * beans reais do Spring, entao a checagem de RequestContext dentro de cada use
 * case e exercida de verdade.
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
class ChecklistExecutionAuthorizationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChecklistExecutionRepository executionRepository;

    @MockitoBean
    private ChecklistTemplateRepository templateRepository;

    @MockitoBean
    private HubRoomProvider hubRoomProvider;

    @MockitoBean
    private HubClassProvider hubClassProvider;

    @MockitoBean
    private HubCourseProvider hubCourseProvider;

    @MockitoBean
    private ChecklistSubmissionWindowRepositoryPort submissionWindowRepository;

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

    private RequestContext teacherOf(UUID classId) {
        return new RequestContext(UUID.randomUUID(), TypeUser.TEACHER, List.of(new ContextClass(classId, ClassRole.TEACHER)));
    }

    private RequestContext representativeOf(UUID userId, UUID classId) {
        return new RequestContext(userId, TypeUser.REPRESENTATIVE, List.of(new ContextClass(classId, ClassRole.REPRESENTATIVE)));
    }

    private RequestContext student() {
        return new RequestContext(UUID.randomUUID(), TypeUser.STUDENT);
    }

    private RequestContext admin() {
        return new RequestContext(UUID.randomUUID(), TypeUser.ADMIN);
    }

    // ---- fixtures -----------------------------------------------------------

    private ChecklistTemplate activeTemplateWithSchema(UUID templateId, UUID roomId) throws Exception {
        ChecklistSchema schema = new ChecklistSchema(List.of(new ChecklistSection(
                "estrutura", "Estrutura", 1,
                List.of(new ChecklistItem("quadro", "Quadro em bom estado?", "Verificar quadro", AnswerType.CONFORMITY, true, 1))
        )));
        Map<String, Object> schemaJson = objectMapper.convertValue(schema, new TypeReference<>() {
        });

        return ChecklistTemplate.builder()
                .id(templateId).roomId(roomId).title("t").description("d").version(1)
                .status(ChecklistTemplateStatus.ACTIVE).active(true)
                .templateGroupId(UUID.randomUUID())
                .schemaJson(schemaJson)
                .build();
    }

    private ChecklistExecution draftExecution(UUID id, ChecklistTemplate template, UUID classId, UUID userId) {
        return ChecklistExecution.builder()
                .id(id).checklistTemplate(template).roomId(template.getRoomId()).classId(classId).userId(userId)
                .status(ChecklistExecutionStatus.DRAFT)
                .answersJson(Map.of())
                .startedAt(LocalDateTime.now())
                .shift(Shift.FULL_AM_PM)
                .checklistType(com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistType.ARRIVAL)
                .build();
    }

    private ChecklistExecution submittedExecution(UUID id, ChecklistTemplate template, UUID classId, UUID userId) {
        ChecklistExecution execution = draftExecution(id, template, classId, userId);
        execution.setStatus(ChecklistExecutionStatus.SUBMITTED);
        execution.setSubmittedAt(LocalDateTime.now());
        return execution;
    }

    private String draftCreateBody(UUID templateId, UUID roomId, UUID classId) {
        return """
                {"templateId":"%s","roomId":"%s","classId":"%s","checklistType":"ARRIVAL"}
                """.formatted(templateId, roomId, classId);
    }

    private String submitBody() {
        return """
                {"answers":[{"itemKey":"quadro","value":"COMPLIANT"}]}
                """;
    }

    private void stubClassAndRoom(UUID roomId, UUID classId) {
        when(hubRoomProvider.existsById(roomId)).thenReturn(true);
        when(hubClassProvider.findById(classId))
                .thenReturn(Optional.of(new ClassReference(classId, "Turma", 1, Shift.FULL_AM_PM, null, null)));
    }

    // ================= 401 sem token =================

    @Test
    void semTokenRetorna401NoCreateDraft() throws Exception {
        mockMvc().perform(post("/api/checklist-executions/drafts")
                        .contentType(APPLICATION_JSON)
                        .content(draftCreateBody(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void semTokenRetorna401NoSubmit() throws Exception {
        mockMvc().perform(post("/api/checklist-executions/" + UUID.randomUUID() + "/submit")
                        .contentType(APPLICATION_JSON).content(submitBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void semTokenRetorna401NoCancel() throws Exception {
        mockMvc().perform(patch("/api/checklist-executions/" + UUID.randomUUID() + "/cancel"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void semTokenRetorna401NoHistorico() throws Exception {
        mockMvc().perform(get("/api/checklist-executions/history/class/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void semTokenRetorna401NoFindById() throws Exception {
        mockMvc().perform(get("/api/checklist-executions/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void semTokenRetorna401NoUpdateAnswers() throws Exception {
        mockMvc().perform(patch("/api/checklist-executions/" + UUID.randomUUID() + "/answers")
                        .contentType(APPLICATION_JSON).content(submitBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void semTokenRetorna401NoListAll() throws Exception {
        mockMvc().perform(get("/api/checklist-executions")).andExpect(status().isUnauthorized());
    }

    // ================= criar draft =================

    @Test
    void representativeDaPropriaTurmaCriaDraft() throws Exception {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ChecklistTemplate template = activeTemplateWithSchema(templateId, roomId);
        stubClassAndRoom(roomId, classId);
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(executionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc().perform(authed(post("/api/checklist-executions/drafts"), representativeOf(userId, classId))
                        .contentType(APPLICATION_JSON).content(draftCreateBody(templateId, roomId, classId)))
                .andExpect(status().isCreated());
    }

    @Test
    void teacherDaPropriaTurmaCriaDraft() throws Exception {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        ChecklistTemplate template = activeTemplateWithSchema(templateId, roomId);
        stubClassAndRoom(roomId, classId);
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(executionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc().perform(authed(post("/api/checklist-executions/drafts"), teacherOf(classId))
                        .contentType(APPLICATION_JSON).content(draftCreateBody(templateId, roomId, classId)))
                .andExpect(status().isCreated());
    }

    @Test
    void studentQueTambemERepresentanteDaTurmaCriaDraft() throws Exception {
        // canActAsRepresentative() aceita TypeUser.STUDENT com ClassRole.REPRESENTATIVE na turma —
        // "representante de turma" no dominio pode ser um STUDENT com esse papel, nao so TypeUser.REPRESENTATIVE.
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ChecklistTemplate template = activeTemplateWithSchema(templateId, roomId);
        stubClassAndRoom(roomId, classId);
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(executionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RequestContext studentRepresentative = new RequestContext(userId, TypeUser.STUDENT,
                List.of(new ContextClass(classId, ClassRole.REPRESENTATIVE)));

        mockMvc().perform(authed(post("/api/checklist-executions/drafts"), studentRepresentative)
                        .contentType(APPLICATION_JSON).content(draftCreateBody(templateId, roomId, classId)))
                .andExpect(status().isCreated());
    }

    @Test
    void teacherDeOutraTurmaNaoCriaDraft() throws Exception {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID requestedClassId = UUID.randomUUID();
        UUID otherClassId = UUID.randomUUID();
        ChecklistTemplate template = activeTemplateWithSchema(templateId, roomId);
        stubClassAndRoom(roomId, requestedClassId);
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));

        mockMvc().perform(authed(post("/api/checklist-executions/drafts"), teacherOf(otherClassId))
                        .contentType(APPLICATION_JSON).content(draftCreateBody(templateId, roomId, requestedClassId)))
                .andExpect(status().isForbidden());
    }

    @Test
    void senaiNaoCriaDraftMesmoComPapelNaTurma() throws Exception {
        // RequestContext.canOperateChecklistExecutionForClass so aceita TEACHER/REPRESENTATIVE/STUDENT —
        // perfil de gestao (SENAI/WEG) e propositalmente excluido, mesmo tendo vinculo de turma.
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        ChecklistTemplate template = activeTemplateWithSchema(templateId, roomId);
        stubClassAndRoom(roomId, classId);
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));

        RequestContext senaiWithClass = new RequestContext(UUID.randomUUID(), TypeUser.SENAI,
                List.of(new ContextClass(classId, ClassRole.TEACHER)));

        mockMvc().perform(authed(post("/api/checklist-executions/drafts"), senaiWithClass)
                        .contentType(APPLICATION_JSON).content(draftCreateBody(templateId, roomId, classId)))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentSemPapelNaoCriaDraft() throws Exception {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        ChecklistTemplate template = activeTemplateWithSchema(templateId, roomId);
        stubClassAndRoom(roomId, classId);
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));

        mockMvc().perform(authed(post("/api/checklist-executions/drafts"), student())
                        .contentType(APPLICATION_JSON).content(draftCreateBody(templateId, roomId, classId)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminNaoCriaDraft() throws Exception {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        ChecklistTemplate template = activeTemplateWithSchema(templateId, roomId);
        stubClassAndRoom(roomId, classId);
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));

        mockMvc().perform(authed(post("/api/checklist-executions/drafts"), admin())
                        .contentType(APPLICATION_JSON).content(draftCreateBody(templateId, roomId, classId)))
                .andExpect(status().isForbidden());
    }

    // ================= submeter =================

    @Test
    void representativeSubmeteAPropriaExecucao() throws Exception {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        ChecklistTemplate template = activeTemplateWithSchema(templateId, roomId);
        ChecklistExecution execution = draftExecution(executionId, template, classId, userId);

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(submissionWindowRepository.findByClassIdAndChecklistType(any(), any())).thenReturn(Optional.empty());
        when(executionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc().perform(authed(post("/api/checklist-executions/" + executionId + "/submit"), representativeOf(userId, classId))
                        .contentType(APPLICATION_JSON).content(submitBody()))
                .andExpect(status().isOk());
    }

    @Test
    void teacherNaoSubmeteExecucaoDeOutroUsuario() throws Exception {
        // Submit exige execution.userId == currentUser.userId, alem da permissao de turma —
        // um TEACHER da turma certa mas que nao criou o draft tambem e negado.
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        ChecklistTemplate template = activeTemplateWithSchema(templateId, roomId);
        ChecklistExecution execution = draftExecution(executionId, template, classId, UUID.randomUUID());

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        mockMvc().perform(authed(post("/api/checklist-executions/" + executionId + "/submit"), teacherOf(classId))
                        .contentType(APPLICATION_JSON).content(submitBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void senaiNaoSubmeteExecucaoAlheia() throws Exception {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        ChecklistTemplate template = activeTemplateWithSchema(templateId, roomId);
        ChecklistExecution execution = draftExecution(executionId, template, classId, UUID.randomUUID());

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        mockMvc().perform(authed(post("/api/checklist-executions/" + executionId + "/submit"), senai())
                        .contentType(APPLICATION_JSON).content(submitBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentNaoSubmeteExecucao() throws Exception {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        ChecklistTemplate template = activeTemplateWithSchema(templateId, roomId);
        ChecklistExecution execution = draftExecution(executionId, template, classId, UUID.randomUUID());

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        mockMvc().perform(authed(post("/api/checklist-executions/" + executionId + "/submit"), student())
                        .contentType(APPLICATION_JSON).content(submitBody()))
                .andExpect(status().isForbidden());
    }

    // ================= cancelar =================
    // REVISAR COM O TIME (ADR-005): a matriz do ADR-005 diz que SENAI/WEG deveriam
    // receber 403 ao cancelar. O codigo atual (RequestContext.canCancelChecklistExecution)
    // da um bypass explicito para canManageChecklistTemplates() ANTES de checar
    // ownership/turma — SENAI/WEG cancelam qualquer execucao de qualquer turma. O
    // teste abaixo documenta o comportamento real do codigo.

    @Test
    void senaiCancelaExecucaoDeQualquerTurma() throws Exception {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        ChecklistTemplate template = activeTemplateWithSchema(templateId, roomId);
        ChecklistExecution execution = submittedExecution(executionId, template, classId, UUID.randomUUID());

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(executionRepository.countByUserIdAndStatus(any(), eq(ChecklistExecutionStatus.SUBMITTED.name()))).thenReturn(0L);
        when(executionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc().perform(authed(patch("/api/checklist-executions/" + executionId + "/cancel"), senai()))
                .andExpect(status().isOk());
    }

    @Test
    void representativeCancelaAPropriaExecucao() throws Exception {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        ChecklistTemplate template = activeTemplateWithSchema(templateId, roomId);
        ChecklistExecution execution = submittedExecution(executionId, template, classId, userId);

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(executionRepository.countByUserIdAndStatus(any(), eq(ChecklistExecutionStatus.SUBMITTED.name()))).thenReturn(0L);
        when(executionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc().perform(authed(patch("/api/checklist-executions/" + executionId + "/cancel"), representativeOf(userId, classId)))
                .andExpect(status().isOk());
    }

    @Test
    void teacherNaoCancelaExecucaoDeOutroUsuario() throws Exception {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        ChecklistTemplate template = activeTemplateWithSchema(templateId, roomId);
        ChecklistExecution execution = submittedExecution(executionId, template, classId, UUID.randomUUID());

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        mockMvc().perform(authed(patch("/api/checklist-executions/" + executionId + "/cancel"), teacherOf(classId)))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentNaoCancelaExecucao() throws Exception {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        ChecklistTemplate template = activeTemplateWithSchema(templateId, roomId);
        ChecklistExecution execution = submittedExecution(executionId, template, classId, UUID.randomUUID());

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        mockMvc().perform(authed(patch("/api/checklist-executions/" + executionId + "/cancel"), student()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCancelaExecucaoDeQualquerTurma() throws Exception {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        ChecklistTemplate template = activeTemplateWithSchema(templateId, roomId);
        ChecklistExecution execution = submittedExecution(executionId, template, classId, UUID.randomUUID());

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(executionRepository.countByUserIdAndStatus(any(), eq(ChecklistExecutionStatus.SUBMITTED.name()))).thenReturn(0L);
        when(executionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc().perform(authed(patch("/api/checklist-executions/" + executionId + "/cancel"), admin()))
                .andExpect(status().isOk());
    }

    // ================= historico da turma =================

    @Test
    void senaiVeHistoricoDeQualquerTurma() throws Exception {
        UUID classId = UUID.randomUUID();
        when(executionRepository.findByClassIdAndStatusOrderBySubmittedAtDesc(eq(classId), any(), any()))
                .thenReturn(Page.empty());

        mockMvc().perform(authed(get("/api/checklist-executions/history/class/" + classId), senai()))
                .andExpect(status().isOk());
    }

    @Test
    void teacherDaPropriaTurmaVeHistorico() throws Exception {
        UUID classId = UUID.randomUUID();
        when(executionRepository.findByClassIdAndStatusOrderBySubmittedAtDesc(eq(classId), any(), any()))
                .thenReturn(Page.empty());

        mockMvc().perform(authed(get("/api/checklist-executions/history/class/" + classId), teacherOf(classId)))
                .andExpect(status().isOk());
    }

    @Test
    void teacherDeOutraTurmaNaoVeHistorico() throws Exception {
        UUID classId = UUID.randomUUID();
        UUID otherClassId = UUID.randomUUID();

        mockMvc().perform(authed(get("/api/checklist-executions/history/class/" + classId), teacherOf(otherClassId)))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentNaoVeHistorico() throws Exception {
        UUID classId = UUID.randomUUID();
        mockMvc().perform(authed(get("/api/checklist-executions/history/class/" + classId), student()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminVeHistoricoDeQualquerTurma() throws Exception {
        UUID classId = UUID.randomUUID();
        when(executionRepository.findByClassIdAndStatusOrderBySubmittedAtDesc(eq(classId), any(), any()))
                .thenReturn(Page.empty());

        mockMvc().perform(authed(get("/api/checklist-executions/history/class/" + classId), admin()))
                .andExpect(status().isOk());
    }

    // ================= buscar execucao por id =================

    @Test
    void teacherDeOutraTurmaNaoVeExecucaoPorId() throws Exception {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID otherClassId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        ChecklistTemplate template = activeTemplateWithSchema(templateId, roomId);
        ChecklistExecution execution = draftExecution(executionId, template, classId, UUID.randomUUID());

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        mockMvc().perform(authed(get("/api/checklist-executions/" + executionId), teacherOf(otherClassId)))
                .andExpect(status().isForbidden());
    }

    @Test
    void teacherDaPropriaTurmaVeExecucaoPorId() throws Exception {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        ChecklistTemplate template = activeTemplateWithSchema(templateId, roomId);
        ChecklistExecution execution = draftExecution(executionId, template, classId, UUID.randomUUID());

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        mockMvc().perform(authed(get("/api/checklist-executions/" + executionId), teacherOf(classId)))
                .andExpect(status().isOk());
    }

    @Test
    void studentNaoVeExecucaoPorId() throws Exception {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        ChecklistTemplate template = activeTemplateWithSchema(templateId, roomId);
        ChecklistExecution execution = draftExecution(executionId, template, classId, UUID.randomUUID());

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        mockMvc().perform(authed(get("/api/checklist-executions/" + executionId), student()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminVeExecucaoPorIdDeQualquerTurma() throws Exception {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        ChecklistTemplate template = activeTemplateWithSchema(templateId, roomId);
        ChecklistExecution execution = draftExecution(executionId, template, classId, UUID.randomUUID());

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        mockMvc().perform(authed(get("/api/checklist-executions/" + executionId), admin()))
                .andExpect(status().isOk());
    }

    // ================= atualizar respostas (checklist ja submetido) =================

    @Test
    void senaiAtualizaRespostasDeQualquerTurma() throws Exception {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        ChecklistTemplate template = activeTemplateWithSchema(templateId, roomId);
        ChecklistExecution execution = submittedExecution(executionId, template, classId, UUID.randomUUID());

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(executionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc().perform(authed(patch("/api/checklist-executions/" + executionId + "/answers"), senai())
                        .contentType(APPLICATION_JSON).content(submitBody()))
                .andExpect(status().isOk());
    }

    @Test
    void teacherDaPropriaTurmaAtualizaRespostas() throws Exception {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        ChecklistTemplate template = activeTemplateWithSchema(templateId, roomId);
        ChecklistExecution execution = submittedExecution(executionId, template, classId, UUID.randomUUID());

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(executionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc().perform(authed(patch("/api/checklist-executions/" + executionId + "/answers"), teacherOf(classId))
                        .contentType(APPLICATION_JSON).content(submitBody()))
                .andExpect(status().isOk());
    }

    @Test
    void teacherDeOutraTurmaNaoAtualizaRespostas() throws Exception {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID otherClassId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        ChecklistTemplate template = activeTemplateWithSchema(templateId, roomId);
        ChecklistExecution execution = submittedExecution(executionId, template, classId, UUID.randomUUID());

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        mockMvc().perform(authed(patch("/api/checklist-executions/" + executionId + "/answers"), teacherOf(otherClassId))
                        .contentType(APPLICATION_JSON).content(submitBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentNaoAtualizaRespostas() throws Exception {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        ChecklistTemplate template = activeTemplateWithSchema(templateId, roomId);
        ChecklistExecution execution = submittedExecution(executionId, template, classId, UUID.randomUUID());

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        mockMvc().perform(authed(patch("/api/checklist-executions/" + executionId + "/answers"), student())
                        .contentType(APPLICATION_JSON).content(submitBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminAtualizaRespostasDeQualquerTurma() throws Exception {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        ChecklistTemplate template = activeTemplateWithSchema(templateId, roomId);
        ChecklistExecution execution = submittedExecution(executionId, template, classId, UUID.randomUUID());

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(executionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc().perform(authed(patch("/api/checklist-executions/" + executionId + "/answers"), admin())
                        .contentType(APPLICATION_JSON).content(submitBody()))
                .andExpect(status().isOk());
    }

    // ================= listar execucoes (sem checagem de perfil) =================
    // NOTA: ListChecklistExecutionsUseCase.execute(Pageable) nao lanca AccessDeniedException
    // para nenhum perfil — ele apenas filtra os resultados (findAll para gestores,
    // findByClassIdIn para os demais, pagina vazia se o usuario nao tem turmas). Nao ha
    // 403 possivel aqui; documentamos o 200 para deixar claro que a ausencia de bloqueio
    // e intencional/atual, nao um buraco de cobertura de teste.

    @Test
    void qualquerAutenticadoListaExecucoes() throws Exception {
        when(templateRepository.findAll()).thenReturn(List.of());
        when(executionRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(executionRepository.findByClassIdIn(any(), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc().perform(authed(get("/api/checklist-executions"), senai())).andExpect(status().isOk());
        mockMvc().perform(authed(get("/api/checklist-executions"), student())).andExpect(status().isOk());
        mockMvc().perform(authed(get("/api/checklist-executions"), admin())).andExpect(status().isOk());
    }
}
