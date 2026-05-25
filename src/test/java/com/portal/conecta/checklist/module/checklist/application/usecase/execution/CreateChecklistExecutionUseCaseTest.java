package com.portal.conecta.checklist.module.checklist.application.usecase.execution;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.module.checklist.domain.enums.Period;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistTemplateRepository;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionDraftCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import com.portal.conecta.checklist.shared.context.CurrentUserClassLink;
import com.portal.conecta.checklist.shared.context.CurrentUserContext;
import com.portal.conecta.checklist.shared.context.CurrentUserProvider;
import com.portal.conecta.checklist.shared.security.HubPermissionVersionValidator;
import com.portal.conecta.checklist.shared.security.StalePermissionVersionException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

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
import static org.mockito.Mockito.doThrow;

class CreateChecklistExecutionUseCaseTest {

    private final ChecklistExecutionRepository executionRepository = mock(ChecklistExecutionRepository.class);
    private final ChecklistTemplateRepository templateRepository = mock(ChecklistTemplateRepository.class);
    private final ChecklistExecutionMapper executionMapper = mock(ChecklistExecutionMapper.class);
    private final CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
    private final HubPermissionVersionValidator permissionVersionValidator = mock(HubPermissionVersionValidator.class);
    private final CreateChecklistExecutionUseCase useCase = new CreateChecklistExecutionUseCase(
            executionRepository,
            templateRepository,
            executionMapper,
            currentUserProvider,
            permissionVersionValidator
    );

    @Test
    @DisplayName("deve criar draft quando template esta ativo e nao existe duplicidade")
    void deveCriarDraftQuandoTemplateEstaAtivoENaoExisteDuplicidade() {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ChecklistExecutionDraftCreateDTO request = request(templateId, roomId, classId);
        ChecklistTemplate template = activeTemplate(templateId, roomId);
        CurrentUserContext currentUser = representative(userId, classId);
        ChecklistExecution draft = ChecklistExecution.builder().id(UUID.randomUUID()).build();
        ChecklistExecution saved = ChecklistExecution.builder().id(UUID.randomUUID()).build();

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(executionRepository.existsDuplicateChecklist(
                eq(classId),
                eq(roomId),
                eq(Period.MORNING.name()),
                eq(ChecklistType.ARRIVAL.name()),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(false);
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(executionMapper.toDraftEntity(eq(request), eq(template), eq(userId), any(LocalDateTime.class))).thenReturn(draft);
        when(executionRepository.save(draft)).thenReturn(saved);

        ChecklistExecution result = useCase.execute(request);

        assertSame(saved, result);
        verify(permissionVersionValidator).validate(currentUser);
        verify(executionMapper).toDraftEntity(eq(request), eq(template), eq(userId), any(LocalDateTime.class));
        verify(executionRepository).save(draft);
    }

    @Test
    @DisplayName("deve rejeitar quando ja existe draft ou envio para o mesmo conjunto")
    void deveRejeitarQuandoJaExisteDraftOuEnvioParaMesmoConjunto() {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        ChecklistExecutionDraftCreateDTO request = request(templateId, roomId, classId);

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(activeTemplate(templateId, roomId)));
        when(currentUserProvider.getCurrentUser()).thenReturn(representative(UUID.randomUUID(), classId));
        when(executionRepository.existsDuplicateChecklist(
                eq(classId),
                eq(roomId),
                eq(Period.MORNING.name()),
                eq(ChecklistType.ARRIVAL.name()),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(request));

        verify(currentUserProvider).getCurrentUser();
        verify(permissionVersionValidator).validate(any(CurrentUserContext.class));
        verify(executionMapper, never()).toDraftEntity(any(), any(), any(), any());
        verify(executionRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve rejeitar quando template nao existe")
    void deveRejeitarQuandoTemplateNaoExiste() {
        ChecklistExecutionDraftCreateDTO request = request(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        when(templateRepository.findById(request.templateId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> useCase.execute(request));

        verify(executionRepository, never()).existsDuplicateChecklist(any(), any(), any(), any(), any(), any());
        verify(executionRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve rejeitar quando template nao esta ativo")
    void deveRejeitarQuandoTemplateNaoEstaAtivo() {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        ChecklistExecutionDraftCreateDTO request = request(templateId, roomId, UUID.randomUUID());
        ChecklistTemplate template = ChecklistTemplate.builder()
                .id(templateId)
                .roomId(roomId)
                .status(ChecklistTemplateStatus.DRAFT)
                .active(false)
                .build();

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));

        assertThrows(IllegalStateException.class, () -> useCase.execute(request));

        verify(executionRepository, never()).existsDuplicateChecklist(any(), any(), any(), any(), any(), any());
        verify(executionRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve rejeitar quando template pertence a outra sala")
    void deveRejeitarQuandoTemplatePertenceAOutraSala() {
        UUID templateId = UUID.randomUUID();
        ChecklistExecutionDraftCreateDTO request = request(templateId, UUID.randomUUID(), UUID.randomUUID());

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(activeTemplate(templateId, UUID.randomUUID())));

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(request));

        verify(executionRepository, never()).existsDuplicateChecklist(any(), any(), any(), any(), any(), any());
        verify(executionRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve rejeitar quando usuario nao representa a turma informada")
    void deveRejeitarQuandoUsuarioNaoRepresentaATurmaInformada() {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID requestedClassId = UUID.randomUUID();
        UUID anotherClassId = UUID.randomUUID();
        ChecklistExecutionDraftCreateDTO request = request(templateId, roomId, requestedClassId);

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(activeTemplate(templateId, roomId)));
        when(currentUserProvider.getCurrentUser()).thenReturn(representative(UUID.randomUUID(), anotherClassId));

        assertThrows(AccessDeniedException.class, () -> useCase.execute(request));

        verify(permissionVersionValidator, never()).validate(any());
        verify(executionRepository, never()).existsDuplicateChecklist(any(), any(), any(), any(), any(), any());
        verify(executionMapper, never()).toDraftEntity(any(), any(), any(), any());
        verify(executionRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve rejeitar quando versao de permissao do token esta desatualizada")
    void deveRejeitarQuandoVersaoDePermissaoDoTokenEstaDesatualizada() {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        CurrentUserContext currentUser = representative(UUID.randomUUID(), classId);
        ChecklistExecutionDraftCreateDTO request = request(templateId, roomId, classId);

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(activeTemplate(templateId, roomId)));
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        doThrow(new StalePermissionVersionException("stale"))
                .when(permissionVersionValidator)
                .validate(currentUser);

        assertThrows(StalePermissionVersionException.class, () -> useCase.execute(request));

        verify(executionRepository, never()).existsDuplicateChecklist(any(), any(), any(), any(), any(), any());
        verify(executionMapper, never()).toDraftEntity(any(), any(), any(), any());
        verify(executionRepository, never()).save(any());
    }

    private ChecklistExecutionDraftCreateDTO request(UUID templateId, UUID roomId, UUID classId) {
        return new ChecklistExecutionDraftCreateDTO(
                templateId,
                roomId,
                classId,
                Period.MORNING,
                ChecklistType.ARRIVAL
        );
    }

    private ChecklistTemplate activeTemplate(UUID templateId, UUID roomId) {
        return ChecklistTemplate.builder()
                .id(templateId)
                .roomId(roomId)
                .status(ChecklistTemplateStatus.ACTIVE)
                .active(true)
                .build();
    }

    private CurrentUserContext representative(UUID userId, UUID classId) {
        return new CurrentUserContext(
                userId,
                "Representante",
                "rep@exemplo.com",
                "aluno",
                List.of(new CurrentUserClassLink(classId.toString(), null, "representante"))
        );
    }
}
