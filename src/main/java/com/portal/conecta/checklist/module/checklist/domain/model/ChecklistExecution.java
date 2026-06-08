package com.portal.conecta.checklist.module.checklist.domain.model;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.module.checklist.domain.enums.Period;
import com.portal.conecta.checklist.module.checklist.domain.enums.Shift;
import com.portal.conecta.checklist.module.issues.domain.model.ChecklistIssue;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Entidade responsável por representar a execução de um checklist.
 * <p>
 * Armazena as informações de uma execução realizada a partir de um
 * {@link ChecklistTemplate}, incluindo respostas fornecidas, status,
 * pontuação de conformidade, pendências geradas e dados de contexto
 * como sala, turma, usuário e período da execução.
 * </p>
 *
 * <p>
 * As respostas do checklist são armazenadas em formato JSON através
 * do campo {@code answersJson}, permitindo flexibilidade na estrutura
 * dos dados respondidos.
 * </p>
 *
 * @author Murilo
 * @since 1.0
 */
@Entity
@Table(name = "checklist_execution")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistExecution {

    /**
     * Identificador único da execução do checklist.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Template utilizado como base para a execução do checklist.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checklist_template_id", nullable = false)
    private ChecklistTemplate checklistTemplate;

    /**
     * Identificador da sala associada à execução.
     */
    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    /**
     * Identificador da turma associada à execução.
     */
    @Column(name = "class_id", nullable = false)
    private UUID classId;

    /**
     * Identificador do usuário responsável pela execução.
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Status atual da execução do checklist.
     * Por padrão inicia como {@link ChecklistExecutionStatus#DRAFT}.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ChecklistExecutionStatus status = ChecklistExecutionStatus.DRAFT;

    /**
     * Respostas fornecidas durante a execução do checklist.
     * Os dados são armazenados em formato JSONB no banco de dados.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "answers_json", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> answersJson;

    /**
     * Percentual ou pontuação de conformidade calculada a partir das respostas.
     */
    @Column(name = "compliance_score", precision = 5, scale = 2)
    private BigDecimal complianceScore;

    /**
     * Lista de pendências geradas durante a execução do checklist.
     */
    @OneToMany(mappedBy = "checklistExecution", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChecklistIssue> issues = new ArrayList<>();

    /**
     * Tipo do checklist executado.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "checklist_type", nullable = false, length = 30)
    private ChecklistType checklistType;

    /**
     * Data e hora de início da execução.
     */
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    /**
     * Data e hora de submissão/finalização da execução.
     */
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    /**
     * Período em que a execução foi realizada.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "period", nullable = false, length = 20)
    private Period period;

    /**
     * Turno da turma no momento da criacao — snapshot para evitar chamada extra ao Hub no submit.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "shift", nullable = false, length = 20)
    private Shift shift;

    /**
     * Adiciona uma pendência à execução do checklist.
     *
     * @param issue pendência a ser associada à execução
     */
    public void addIssue(ChecklistIssue issue) {
        issues.add(issue);
        issue.setChecklistExecution(this);
    }
}