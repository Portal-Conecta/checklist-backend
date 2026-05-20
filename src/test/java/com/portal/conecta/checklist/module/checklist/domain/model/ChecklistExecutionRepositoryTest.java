package com.portal.conecta.checklist.module.checklist.domain.model;

import com.portal.conecta.checklist.module.checklist.domain.valueobject.ClassReference;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.RoomReference;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.UserReference;
import com.portal.conecta.checklist.module.checklist.presentation.dto.ClassList;
import com.portal.conecta.checklist.module.checklist.presentation.dto.UserToken;
import com.portal.conecta.checklist.module.issues.domain.model.ChecklistIssue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChecklistExecutionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Deve persistir e recuperar um ChecklistExecution com relacionamentos, Embedded e JSONB com sucesso")
    void devePersistirERecuperarChecklistExecution() {
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ChecklistTemplate template = criarTemplate(roomId, classId, userId);

        Map<String, Object> answersJson = new LinkedHashMap<>();
        answersJson.put("porta_funcionando", true);
        answersJson.put("quantidade_cadeiras", 30);
        answersJson.put("observacao", "Sala em bom estado");

        ChecklistExecution execution = ChecklistExecution.builder()
                .checklistTemplate(template)
                .roomReference(new RoomReference(roomId))
                .classReference(new ClassReference(classId))
                .userReference(new UserReference(userId))
                .status(Status.SUBMITTED)
                .answersJson(answersJson)
                .complianceScore(new BigDecimal("87.50"))
                .build();

        execution.addIssue(ChecklistIssue.builder()
                .itemKey("projetor_funcionando")
                .description("Projetor nao liga")
                .resolved(false)
                .build());

        ChecklistExecution executionSalva = entityManager.persistAndFlush(execution);
        entityManager.clear();

        ChecklistExecution executionBuscada = entityManager.find(ChecklistExecution.class, executionSalva.getId());

        assertThat(executionBuscada).isNotNull();
        assertThat(executionBuscada.getChecklistTemplate().getId()).isEqualTo(template.getId());
        assertThat(executionBuscada.getRoomReference().getRoomid()).isEqualTo(roomId);
        assertThat(executionBuscada.getClassReference().getClassId()).isEqualTo(classId);
        assertThat(executionBuscada.getUserReference().getUserId()).isEqualTo(userId);
        assertThat(executionBuscada.getStatus()).isEqualTo(Status.SUBMITTED);
        assertThat(executionBuscada.getComplianceScore()).isEqualByComparingTo("87.50");

        assertThat(executionBuscada.getAnswersJson()).isNotNull();
        assertThat(executionBuscada.getAnswersJson().get("porta_funcionando")).isEqualTo(true);
        assertThat(executionBuscada.getAnswersJson().get("quantidade_cadeiras")).isEqualTo(30);
        assertThat(executionBuscada.getAnswersJson().get("observacao")).isEqualTo("Sala em bom estado");

        assertThat(executionBuscada.getIssues()).hasSize(1);
        assertThat(executionBuscada.getIssues().get(0).getItemKey()).isEqualTo("projetor_funcionando");
        assertThat(executionBuscada.getIssues().get(0).isResolved()).isFalse();
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
                .status(Status.DRAFT)
                .active(true)
                .roomReference(new RoomReference(roomId))
                .schemaJson(userTokenJson)
                .build();

        return entityManager.persistAndFlush(template);
    }
}
