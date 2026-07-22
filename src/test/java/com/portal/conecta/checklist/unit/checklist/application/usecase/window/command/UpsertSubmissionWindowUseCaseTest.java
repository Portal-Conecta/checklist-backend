package com.portal.conecta.checklist.unit.checklist.application.usecase.window.command;

import com.portal.conecta.checklist.module.checklist.application.usecase.window.command.upsert.UpsertSubmissionWindowUseCase;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.module.checklist.domain.enums.Shift;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistSubmissionWindow;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.ClassReference;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistSubmissionWindowRepository;
import com.portal.conecta.checklist.module.checklist.application.usecase.window.command.upsert.UpsertSubmissionWindowCommand;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.context.TypeUser;
import com.portal.conecta.checklist.module.checklist.application.port.out.integration.HubClassProvider;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpsertSubmissionWindowUseCaseTest {

    private final ChecklistSubmissionWindowRepository repository = mock(ChecklistSubmissionWindowRepository.class);
    private final RequestContextProvider contextProvider = mock(RequestContextProvider.class);
    private final HubClassProvider hubClassProvider = mock(HubClassProvider.class);
    private final UpsertSubmissionWindowUseCase useCase = new UpsertSubmissionWindowUseCase(
            repository,
            contextProvider,
            hubClassProvider
    );

    @Test
    @DisplayName("deve criar janela quando usuario SENAI informa combinacao nova")
    void deveCriarJanelaQuandoUsuarioSenaiInformaCombinacaoNova() {
        UUID classId = UUID.randomUUID();
        UpsertSubmissionWindowCommand request = new UpsertSubmissionWindowCommand(LocalTime.of(7, 30), 30);

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));
        when(hubClassProvider.findById(classId)).thenReturn(Optional.of(classReference(classId, Shift.FULL_AM_PM)));
        when(repository.findByClassIdAndChecklistType(classId, ChecklistType.ARRIVAL))
                .thenReturn(Optional.empty());
        when(repository.save(any(ChecklistSubmissionWindow.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChecklistSubmissionWindow result = useCase.execute(classId, ChecklistType.ARRIVAL, request);

        assertEquals(classId, result.getClassId());
        assertEquals(Shift.FULL_AM_PM, result.getShift());
        assertEquals(ChecklistType.ARRIVAL, result.getChecklistType());
        assertEquals(LocalTime.of(7, 30), result.getOpenAt());
        assertEquals(30, result.getDurationMinutes());
        verify(repository).save(any(ChecklistSubmissionWindow.class));
    }

    @Test
    @DisplayName("deve atualizar janela existente quando usuario WEG informa combinacao ja cadastrada")
    void deveAtualizarJanelaExistenteQuandoUsuarioWegInformaCombinacaoJaCadastrada() {
        UUID classId = UUID.randomUUID();
        ChecklistSubmissionWindow existing = ChecklistSubmissionWindow.builder()
                .classId(classId)
                .shift(Shift.FULL_AM_PM)
                .checklistType(ChecklistType.POST_BREAK)
                .openAt(LocalTime.of(17, 30))
                .durationMinutes(30)
                .build();
        UpsertSubmissionWindowCommand request = new UpsertSubmissionWindowCommand(LocalTime.of(18, 0), 45);

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.WEG));
        when(hubClassProvider.findById(classId)).thenReturn(Optional.of(classReference(classId, Shift.FULL_PM_NT)));
        when(repository.findByClassIdAndChecklistType(classId, ChecklistType.POST_BREAK))
                .thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        ChecklistSubmissionWindow result = useCase.execute(classId, ChecklistType.POST_BREAK, request);

        assertSame(existing, result);
        assertEquals(classId, result.getClassId());
        assertEquals(Shift.FULL_PM_NT, result.getShift());
        assertEquals(LocalTime.of(18, 0), result.getOpenAt());
        assertEquals(45, result.getDurationMinutes());
        verify(repository).save(existing);
    }

    @Test
    @DisplayName("deve permitir horarios diferentes para turmas distintas do mesmo tipo")
    void devePermitirHorariosDiferentesParaTurmasDistintasDoMesmoTipo() {
        UUID morningClassId = UUID.randomUUID();
        UUID afternoonClassId = UUID.randomUUID();
        UpsertSubmissionWindowCommand morningRequest = new UpsertSubmissionWindowCommand(LocalTime.of(7, 30), 30);
        UpsertSubmissionWindowCommand afternoonRequest = new UpsertSubmissionWindowCommand(LocalTime.of(14, 45), 25);

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));
        when(hubClassProvider.findById(morningClassId))
                .thenReturn(Optional.of(classReference(morningClassId, Shift.FULL_AM_PM)));
        when(hubClassProvider.findById(afternoonClassId))
                .thenReturn(Optional.of(classReference(afternoonClassId, Shift.FULL_PM_NT)));
        when(repository.findByClassIdAndChecklistType(morningClassId, ChecklistType.ARRIVAL))
                .thenReturn(Optional.empty());
        when(repository.findByClassIdAndChecklistType(afternoonClassId, ChecklistType.ARRIVAL))
                .thenReturn(Optional.empty());
        when(repository.save(any(ChecklistSubmissionWindow.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChecklistSubmissionWindow morningWindow = useCase.execute(morningClassId, ChecklistType.ARRIVAL, morningRequest);
        ChecklistSubmissionWindow afternoonWindow = useCase.execute(afternoonClassId, ChecklistType.ARRIVAL, afternoonRequest);

        assertEquals(morningClassId, morningWindow.getClassId());
        assertEquals(LocalTime.of(7, 30), morningWindow.getOpenAt());
        assertEquals(afternoonClassId, afternoonWindow.getClassId());
        assertEquals(LocalTime.of(14, 45), afternoonWindow.getOpenAt());
    }

    @Test
    @DisplayName("deve rejeitar usuario sem perfil de gestao")
    void deveRejeitarUsuarioSemPerfilDeGestao() {
        UUID classId = UUID.randomUUID();
        UpsertSubmissionWindowCommand request = new UpsertSubmissionWindowCommand(LocalTime.of(7, 30), 30);

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.REPRESENTATIVE));

        assertThrows(AccessDeniedException.class,
                () -> useCase.execute(classId, ChecklistType.ARRIVAL, request));

        verify(hubClassProvider, never()).findById(any());
        verify(repository, never()).findByClassIdAndChecklistType(any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("deve rejeitar janela que ultrapassa meia-noite")
    void deveRejeitarJanelaQueUltrapassaMeiaNoite() {
        UUID classId = UUID.randomUUID();
        UpsertSubmissionWindowCommand request = new UpsertSubmissionWindowCommand(LocalTime.of(23, 45), 30);

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(classId, ChecklistType.ARRIVAL, request));

        verify(hubClassProvider, never()).findById(any());
        verify(repository, never()).findByClassIdAndChecklistType(any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("deve rejeitar quando turma nao existe no Hub")
    void deveRejeitarQuandoTurmaNaoExisteNoHub() {
        UUID classId = UUID.randomUUID();
        UpsertSubmissionWindowCommand request = new UpsertSubmissionWindowCommand(LocalTime.of(7, 30), 30);

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));
        when(hubClassProvider.findById(classId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> useCase.execute(classId, ChecklistType.ARRIVAL, request));

        verify(repository, never()).findByClassIdAndChecklistType(any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("deve rejeitar quando Hub nao informa turno da turma")
    void deveRejeitarQuandoHubNaoInformaTurnoDaTurma() {
        UUID classId = UUID.randomUUID();
        UpsertSubmissionWindowCommand request = new UpsertSubmissionWindowCommand(LocalTime.of(7, 30), 30);

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));
        when(hubClassProvider.findById(classId)).thenReturn(Optional.of(new ClassReference(classId)));

        assertThrows(IllegalStateException.class,
                () -> useCase.execute(classId, ChecklistType.ARRIVAL, request));

        verify(repository, never()).findByClassIdAndChecklistType(any(), any());
        verify(repository, never()).save(any());
    }

    private RequestContext user(TypeUser userType) {
        return new RequestContext(UUID.randomUUID(), userType);
    }

    private ClassReference classReference(UUID classId, Shift shift) {
        return new ClassReference(classId, "Turma teste", 1, shift, null, null);
    }
}
