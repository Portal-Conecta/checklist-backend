package com.portal.conecta.checklist.unit.checklist.application.usecase.execution.command;

import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.create.CreateChecklistExecutionCommand;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.create.CreateChecklistExecutionUseCase;
import com.portal.conecta.checklist.modules.checklist.application.service.window.SubmissionWindowValidator;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.modules.checklist.domain.enums.Period;
import com.portal.conecta.checklist.modules.checklist.domain.enums.Shift;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.modules.checklist.domain.valueobject.ClassReference;
import com.portal.conecta.checklist.modules.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.modules.checklist.infrastructure.persistence.ChecklistTemplateRepository;
import com.portal.conecta.checklist.modules.checklist.application.service.execution.ChecklistExecutionDataMapper;
import com.portal.conecta.checklist.shared.context.ClassRole;
import com.portal.conecta.checklist.shared.context.ContextClass;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.context.TypeUser;
import com.portal.conecta.checklist.modules.checklist.application.port.out.integration.HubClassProvider;
import com.portal.conecta.checklist.modules.checklist.application.port.out.integration.HubCourseProvider;
import com.portal.conecta.checklist.modules.checklist.application.port.out.integration.HubRoomProvider;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateChecklistExecutionUseCaseTest {

    private final ChecklistExecutionRepository executionRepository = mock(ChecklistExecutionRepository.class);
    private final ChecklistTemplateRepository templateRepository   = mock(ChecklistTemplateRepository.class);
    private final ChecklistExecutionDataMapper executionMapper         = mock(ChecklistExecutionDataMapper.class);
    private final RequestContextProvider contextProvider           = mock(RequestContextProvider.class);
    private final HubRoomProvider hubRoomProvider                 = mock(HubRoomProvider.class);
    private final HubClassProvider hubClassProvider               = mock(HubClassProvider.class);
    private final HubCourseProvider hubCourseProvider             = mock(HubCourseProvider.class);
    private final SubmissionWindowValidator submissionWindowValidator = mock(SubmissionWindowValidator.class);

    private CreateChecklistExecutionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateChecklistExecutionUseCase(
                executionRepository,
                templateRepository,
                executionMapper,
                contextProvider,
                hubRoomProvider,
                hubClassProvider,
                hubCourseProvider,
                submissionWindowValidator
        );
        ReflectionTestUtils.setField(useCase, "timezone", "America/Sao_Paulo");
    }

    @Test
    @DisplayName("deve criar draft quando template esta ativo e nao existe duplicidade")
    void deveCriarDraftQuandoTemplateEstaAtivoENaoExisteDuplicidade() {
        UUID templateId = UUID.randomUUID();
        UUID classId    = UUID.randomUUID();
        UUID userId     = UUID.randomUUID();
        CreateChecklistExecutionCommand request = request(templateId, classId);
        ChecklistTemplate template = activeTemplate(templateId, roomId);
        RequestContext currentUser = representative(userId, classId);
        ChecklistExecution draft = ChecklistExecution.builder().id(UUID.randomUUID()).build();
        ChecklistExecution saved = ChecklistExecution.builder().id(UUID.randomUUID()).build();

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(hubRoomProvider.existsById(roomId)).thenReturn(true);
        when(hubClassProvider.findById(classId)).thenReturn(Optional.of(classReference(classId)));
        when(contextProvider.getRequestContext()).thenReturn(currentUser);
        when(executionRepository.existsDuplicateChecklist(
                eq(classId), eq(roomId),
                eq(Period.MORNING.name()), eq(ChecklistType.ARRIVAL.name()),
                any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(false);
        when(executionMapper.toDraftEntity(
                eq(request), eq(template), eq(userId),
                any(LocalDateTime.class), eq(Shift.FULL_AM_PM), eq(Period.MORNING)
        )).thenReturn(draft);
        when(executionRepository.save(draft)).thenReturn(saved);

        ChecklistExecution result = useCase.execute(request);

        assertSame(saved, result);
        verify(executionMapper).toDraftEntity(
                eq(request), eq(template), eq(userId),
                any(LocalDateTime.class), eq(Shift.FULL_AM_PM), eq(Period.MORNING));
        verify(executionRepository).save(draft);
    }

    @Test
    @DisplayName("deve rejeitar quando ja existe draft ou envio para o mesmo conjunto")
    void deveRejeitarQuandoJaExisteDraftOuEnvioParaMesmoConjunto() {
        UUID templateId = UUID.randomUUID();
        UUID roomId     = UUID.randomUUID();
        UUID classId    = UUID.randomUUID();
        CreateChecklistExecutionCommand request = request(templateId, classId);

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(activeTemplate(templateId, roomId)));
        when(hubRoomProvider.existsById(roomId)).thenReturn(true);
        when(hubClassProvider.findById(classId)).thenReturn(Optional.of(classReference(classId)));
        when(contextProvider.getRequestContext()).thenReturn(representative(UUID.randomUUID(), classId));
        when(executionRepository.existsDuplicateChecklist(
                eq(classId), eq(roomId),
                eq(Period.MORNING.name()), eq(ChecklistType.ARRIVAL.name()),
                any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(request));

        verify(contextProvider).getRequestContext();
        verify(executionMapper, never()).toDraftEntity(any(), any(), any(), any(), any(), any());
        verify(executionRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve rejeitar quando sala nao existe no Hub")
    void deveRejeitarQuandoSalaNaoExisteNoHub() {
        UUID templateId = UUID.randomUUID();
        UUID roomId     = UUID.randomUUID();
        UUID classId    = UUID.randomUUID();
        CreateChecklistExecutionCommand request = request(templateId, classId);

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(activeTemplate(templateId, roomId)));
        when(hubRoomProvider.existsById(roomId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> useCase.execute(request));

        verify(hubClassProvider, never()).findById(any());
        verify(contextProvider, never()).getRequestContext();
        verify(executionRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve rejeitar quando turma nao existe no Hub")
    void deveRejeitarQuandoTurmaNaoExisteNoHub() {
        UUID templateId = UUID.randomUUID();
        UUID roomId     = UUID.randomUUID();
        UUID classId    = UUID.randomUUID();
        CreateChecklistExecutionCommand request = request(templateId, classId);

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(activeTemplate(templateId, roomId)));
        when(hubRoomProvider.existsById(roomId)).thenReturn(true);
        when(hubClassProvider.findById(classId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> useCase.execute(request));

        verify(contextProvider, never()).getRequestContext();
        verify(executionRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve rejeitar quando Hub nao informa o turno da turma")
    void deveRejeitarQuandoHubNaoInformaTurnoDaTurma() {
        UUID templateId = UUID.randomUUID();
        UUID roomId     = UUID.randomUUID();
        UUID classId    = UUID.randomUUID();
        CreateChecklistExecutionCommand request = request(templateId, classId);

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(activeTemplate(templateId, roomId)));
        when(hubRoomProvider.existsById(roomId)).thenReturn(true);
        when(hubClassProvider.findById(classId)).thenReturn(Optional.of(new ClassReference(classId)));

        assertThrows(IllegalStateException.class, () -> useCase.execute(request));

        verify(contextProvider, never()).getRequestContext();
        verify(executionRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve rejeitar quando template nao existe")
    void deveRejeitarQuandoTemplateNaoExiste() {
        CreateChecklistExecutionCommand request = request(UUID.randomUUID(), UUID.randomUUID());

        when(templateRepository.findById(request.templateId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> useCase.execute(request));

        verify(executionRepository, never()).existsDuplicateChecklist(any(), any(), any(), any(), any(), any());
        verify(executionRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve rejeitar quando template nao esta ativo")
    void deveRejeitarQuandoTemplateNaoEstaAtivo() {
        UUID templateId = UUID.randomUUID();
        UUID classId    = UUID.randomUUID();
        CreateChecklistExecutionCommand request = request(templateId, classId);
        ChecklistTemplate template = ChecklistTemplate.builder()
                .id(templateId)
                .roomId(UUID.randomUUID())
                .status(ChecklistTemplateStatus.DRAFT)
                .active(false)
                .build();

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));

        assertThrows(IllegalStateException.class, () -> useCase.execute(request));

        verify(executionRepository, never()).existsDuplicateChecklist(any(), any(), any(), any(), any(), any());
        verify(executionRepository, never()).save(any());
    }

    
    @Test
    @DisplayName("deve rejeitar quando usuario nao representa a turma informada")
    void deveRejeitarQuandoUsuarioNaoRepresentaATurmaInformada() {
        UUID templateId      = UUID.randomUUID();
        UUID roomId          = UUID.randomUUID();
        UUID requestedClassId = UUID.randomUUID();
        UUID anotherClassId  = UUID.randomUUID();
        CreateChecklistExecutionCommand request = request(templateId, roomId, requestedClassId);

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(activeTemplate(templateId, roomId)));
        when(hubRoomProvider.existsById(roomId)).thenReturn(true);
        when(hubClassProvider.findById(requestedClassId)).thenReturn(Optional.of(classReference(requestedClassId)));
        when(contextProvider.getRequestContext()).thenReturn(representative(UUID.randomUUID(), anotherClassId));

        assertThrows(AccessDeniedException.class, () -> useCase.execute(request));

        verify(executionRepository, never()).existsDuplicateChecklist(any(), any(), any(), any(), any(), any());
        verify(executionMapper, never()).toDraftEntity(any(), any(), any(), any(), any(), any());
        verify(executionRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve rejeitar perfil de gestao mesmo com papel na turma")
    void deveRejeitarPerfilDeGestaoMesmoComPapelNaTurma() {
        UUID templateId = UUID.randomUUID();
        UUID roomId     = UUID.randomUUID();
        UUID classId    = UUID.randomUUID();
        CreateChecklistExecutionCommand request = request(templateId, roomId, classId);

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(activeTemplate(templateId, roomId)));
        when(hubRoomProvider.existsById(roomId)).thenReturn(true);
        when(hubClassProvider.findById(classId)).thenReturn(Optional.of(classReference(classId)));
        when(contextProvider.getRequestContext()).thenReturn(new RequestContext(
                UUID.randomUUID(),
                TypeUser.SENAI,
                List.of(new ContextClass(classId, ClassRole.TEACHER))
        ));

        assertThrows(AccessDeniedException.class, () -> useCase.execute(request));

        verify(executionRepository, never()).existsDuplicateChecklist(any(), any(), any(), any(), any(), any());
        verify(executionMapper, never()).toDraftEntity(any(), any(), any(), any(), any(), any());
        verify(executionRepository, never()).save(any());
    }

    private CreateChecklistExecutionCommand request(UUID templateId, UUID classId) {
        return new CreateChecklistExecutionCommand(templateId, classId, ChecklistType.ARRIVAL);
    }

    private ChecklistTemplate activeTemplate(UUID templateId, UUID roomId) {
        return ChecklistTemplate.builder()
                .id(templateId)
                .roomId(roomId)
                .status(ChecklistTemplateStatus.ACTIVE)
                .active(true)
                .build();
    }

    private ClassReference classReference(UUID classId) {
        return new ClassReference(classId, "Turma teste", 1, Shift.FULL_AM_PM, null, null);
    }

    private RequestContext representative(UUID userId, UUID classId) {
        return new RequestContext(
                userId,
                TypeUser.REPRESENTATIVE,
                List.of(new ContextClass(classId, ClassRole.REPRESENTATIVE))
        );
    }
}
