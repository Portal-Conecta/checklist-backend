package com.portal.conecta.checklist.module.issues.domain.model;

import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.ClassReference;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.RoomReference;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.UserReference;
import com.portal.conecta.checklist.module.checklist.presentation.dto.ClassList;
import com.portal.conecta.checklist.module.checklist.presentation.dto.UserToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChecklistIssueRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Deve persistir e recuperar uma ChecklistIssue atribuida a um usuario")
    void devePersistirERecuperarChecklistIssueAtribuidaAUsuario() {
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID assignedUserId = UUID.randomUUID();
        Instant dueAt = Instant.now().plus(2, ChronoUnit.DAYS);

        ChecklistExecution execution = criarExecution(roomId, classId, userId);

        ChecklistIssue issue = ChecklistIssue.builder()
                .checklistExecution(execution)
                .assignedUserReference(new UserReference(assignedUserId))
                .itemKey("porta_funcionando")
                .itemTitleSnapshot("Porta funcionando")
                .title("Porta com problema")
                .description("A porta nao esta fechando corretamente")
                .status(Status.OPEN)
                .priority(Priority.HIGH)
                .dueAt(dueAt)
                .build();

        ChecklistIssue issueSalva = entityManager.persistAndFlush(issue);
        entityManager.clear();

        ChecklistIssue issueBuscada = entityManager.find(ChecklistIssue.class, issueSalva.getId());

        assertThat(issueBuscada).isNotNull();
        assertThat(issueBuscada.getChecklistExecution().getId()).isEqualTo(execution.getId());
        assertThat(issueBuscada.getAssignedUserReference().getUserId()).isEqualTo(assignedUserId);
        assertThat(issueBuscada.getItemKey()).isEqualTo("porta_funcionando");
        assertThat(issueBuscada.getItemTitleSnapshot()).isEqualTo("Porta funcionando");
        assertThat(issueBuscada.getTitle()).isEqualTo("Porta com problema");
        assertThat(issueBuscada.getDescription()).isEqualTo("A porta nao esta fechando corretamente");
        assertThat(issueBuscada.getStatus()).isEqualTo(Status.OPEN);
        assertThat(issueBuscada.getPriority()).isEqualTo(Priority.HIGH);
        assertThat(issueBuscada.getDueAt()).isNotNull();
        assertThat(issueBuscada.getResolvedAt()).isNull();
    }

    @Test
    @DisplayName("Deve preencher resolvedAt ao resolver uma ChecklistIssue")
    void devePreencherResolvedAtAoResolverChecklistIssue() {
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ChecklistIssue issue = ChecklistIssue.builder()
                .checklistExecution(criarExecution(roomId, classId, userId))
                .assignedUserReference(new UserReference(userId))
                .itemKey("projetor_funcionando")
                .itemTitleSnapshot("Projetor funcionando")
                .title("Projetor com defeito")
                .description("Projetor nao liga")
                .dueAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .build();

        issue.resolve();

        ChecklistIssue issueSalva = entityManager.persistAndFlush(issue);
        entityManager.clear();

        ChecklistIssue issueBuscada = entityManager.find(ChecklistIssue.class, issueSalva.getId());

        assertThat(issueBuscada.getStatus()).isEqualTo(Status.RESOLVED);
        assertThat(issueBuscada.getPriority()).isEqualTo(Priority.MEDIUM);
        assertThat(issueBuscada.getResolvedAt()).isNotNull();
    }

    private ChecklistExecution criarExecution(UUID roomId, UUID classId, UUID userId) {
        ChecklistTemplate template = criarTemplate(roomId, classId, userId);

        Map<String, Object> answersJson = new LinkedHashMap<>();
        answersJson.put("porta_funcionando", false);

        ChecklistExecution execution = ChecklistExecution.builder()
                .checklistTemplate(template)
                .roomReference(new RoomReference(roomId))
                .classReference(new ClassReference(classId))
                .userReference(new UserReference(userId))
                .status(com.portal.conecta.checklist.module.checklist.domain.model.Status.SUBMITTED)
                .answersJson(answersJson)
                .complianceScore(new BigDecimal("80.00"))
                .build();

        return entityManager.persistAndFlush(execution);
    }

    private ChecklistTemplate criarTemplate(UUID roomId, UUID classId, UUID userId) {
        ClassList turma = new ClassList(classId, "aluno", "representante");

        UserToken userTokenJson = new UserToken(
                userId,
                "Joao Silva",
                "joao@exemplo.com",
                "aluno",
                List.of(turma),
                1710000000L,
                1710003600L
        );

        ChecklistTemplate template = ChecklistTemplate.builder()
                .title("Checklist de Entrada")
                .description("Verificacao de rotina")
                .version(1)
                .status(com.portal.conecta.checklist.module.checklist.domain.model.Status.DRAFT)
                .active(true)
                .roomReference(new RoomReference(roomId))
                .schemaJson(userTokenJson)
                .build();

        return entityManager.persistAndFlush(template);
    }
}
