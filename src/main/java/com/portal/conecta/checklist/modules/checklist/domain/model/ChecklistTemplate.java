package com.portal.conecta.checklist.modules.checklist.domain.model;

import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistTemplateStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * <h6>Entidade JPA que representa um {@code checklist_template} no banco de dados.</h6>
 *
 * <p>Um template de checklist define a estrutura(schema) de um formulário vinculado a uma sala ({@code roomId}). Ele pode estar em rascunho, ativo ou arquivado, e passa por versionamento a cada alteração publicada. </p>
 * @see ChecklistTemplateStatus
 */

@Entity
@Table(name = "checklist_template")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistTemplate {

    /**
     * Identificador único do template, gerado automaticamente como {@code UUID.}
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Agrupa todas as versões do mesmo template.
     * Gerado na criação e herdado por todas as versões subsequentes.
     */
    @Column(name = "template_group_id", nullable = false)
    private UUID templateGroupId;

    /**
     * Identificador da sala à qual este template pertence.
     * <br>
     * <b>Não pode ser nulo.</b>
     */
    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    /**
     * Título do template.
     * <br>
     * <b>Máximo de 150 caracteres, obrigatório.</b>
     */
    @Column(name = "title", nullable = false, length = 150)
    private String title;

    /**
     * Descrição resumida do propósito do template.
     * <br>
     * <b>Máximo de 250 caracteres, obrigatório.</b>
     */
    @Column(name = "description", nullable = false, length = 250)
    private String description;

    /**
     * Número da versão do template.
     * <br>
     * <b>Incrementado a cada nova publicação do mesmo template.</b>
     */
    @Column(name = "version", nullable = false)
    private int version;

    /**
     * Status atual do template.
     * <br>
     * <b>O valor padrão é {@link  ChecklistTemplateStatus#DRAFT}</b>
     * @see ChecklistTemplateStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ChecklistTemplateStatus status = ChecklistTemplateStatus.DRAFT;

    /**
     * Indica se o template está ativo no sistema.
     * <br>
     * <b>Templates inativos não devem ser exibidos ou utilizados em novos checklists.</b>
     */
    @Column(name = "active", nullable = false)
    private boolean active;

    /**
     * Esquema dinâmico do formulário, armazenado como {@code JSONB } no PostgreSQL.
     * <b>Define os campos, tipos e validações do checklist. A estrutura é livre e interpretada pelo frontend em tempo de execução</b>
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "schema_json", nullable = false)
    private Map<String, Object> schemaJson;

    /**
     * Data e hora de criação do template, preenchida automaticamente pelo Hibernate.
     * <br>
     * <b> Esse campo não pode ser atualizado após a criação.</b>
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Data e hora da última atualização do template.
     * <br>
     * Preenchida automaticamente pelo método {@link #markUpdatedAt()}.
     * <br> <br>
     * <b>Pode ser nulo se o template nunca foi atualizado.</b>
     */
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Atualiza o campo {@code updateAt} para o instante atual.
     *<br>
     * <b>Invocado automaticamnte pelo JPA antes de qualquer operação de {@code  UPDATE} no banco de dados.</b>
     */
    @PreUpdate
    public void markUpdatedAt() {
        this.updatedAt = Instant.now();
    }
}
