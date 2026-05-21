package com.portal.conecta.checklist.module.checklist.domain.model;

import com.portal.conecta.checklist.module.checklist.domain.valueobject.RoomReference;
import com.portal.conecta.checklist.module.checklist.presentation.dto.ClassList;
import com.portal.conecta.checklist.module.checklist.presentation.dto.UserToken;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@Disabled("Falhando porque não há datasource de teste configurado")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChecklistTemplateRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Deve persistir e recuperar um ChecklistTemplate com Embedded e JSONB com sucesso")
    void devePersistirERecuperarChecklistTemplate() {
        ClassList turma = new ClassList(UUID.randomUUID(), "aluno", "representante");

        UserToken userTokenJson = new UserToken(
                UUID.randomUUID(),
                "João Silva",
                "joao@exemplo.com",
                "aluno",
                List.of(turma),
                1710000000L,
                1710003600L
        );

        RoomReference roomReference = new RoomReference(UUID.randomUUID());

        ChecklistTemplate template = ChecklistTemplate.builder()
                .title("Checklist de Entrada")
                .description("Verificação de rotina")
                .version(1)
                .active(true)
                .status(Status.DRAFT)
                .roomId(roomReference.getRoomid())
                .schemaJson(userTokenJson)
                .build();

        ChecklistTemplate templateSalvo = entityManager.persistAndFlush(template);
        entityManager.clear();

        ChecklistTemplate templateBuscado = entityManager.find(ChecklistTemplate.class, templateSalvo.getId());

        assertThat(templateBuscado).isNotNull();
        assertThat(templateBuscado.getTitle()).isEqualTo("Checklist de Entrada");
        assertThat(templateBuscado.getRoomId()).isEqualTo(roomReference.getRoomid());

        assertThat(templateBuscado.getSchemaJson()).isNotNull();
        assertThat(templateBuscado.getSchemaJson().name()).isEqualTo("João Silva");
        assertThat(templateBuscado.getSchemaJson().classList()).hasSize(1);
        assertThat(templateBuscado.getSchemaJson().classList().get(0).papelNaTurma()).isEqualTo("representante");
    }
}
