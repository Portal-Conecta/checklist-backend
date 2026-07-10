package com.portal.conecta.checklist.modules.checklist.issues.domain.model;

import com.portal.conecta.checklist.modules.checklist.domain.valueobject.UserReference;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.modules.checklist.issues.domain.enums.IssuePriority;
import com.portal.conecta.checklist.modules.checklist.issues.domain.enums.IssueStatus;
import com.portal.conecta.checklist.modules.checklist.issues.domain.exception.InvalidIssueTransitionException;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidade que representa uma pendencia gerada por item nao conforme.
 *
 * <p>Relaciona a issue com a execucao do checklist, usuario responsavel, item
 * afetado, prioridade, status e prazo de resolucao.</p>
 *
 * <p>Transicoes validas:</p>
 * <ul>
 *   <li>OPEN → IN_PROGRESS ({@link #startProgress()})</li>
 *   <li>IN_PROGRESS → RESOLVED ({@link #resolve()})</li>
 *   <li>RESOLVED → VALIDATED ({@link #validate()})</li>
 *   <li>RESOLVED → REOPENED ({@link #reopen()})</li>
 *   <li>REOPENED → IN_PROGRESS ({@link #restartProgress()})</li>
 *   <li>OPEN → CANCELED ({@link #cancel()})</li>
 *   <li>IN_PROGRESS → CANCELED ({@link #cancel()})</li>
 * </ul>
 */
@Entity
@Table(name = "checklist_issue")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checklist_execution_id", nullable = false)
    private ChecklistExecution checklistExecution;

    @Embedded
    @AttributeOverride(name = "userId", column = @Column(name = "assigned_user_id", nullable = false))
    private UserReference assignedUserReference;

    @Column(name = "item_key", nullable = false, length = 150)
    private String itemKey;

    @Column(name = "item_title_snapshot", nullable = false, length = 150)
    private String itemTitleSnapshot;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private IssueStatus status = IssueStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    @Builder.Default
    private IssuePriority priority = IssuePriority.MEDIUM;

    @Column(name = "due_at", nullable = false)
    private Instant dueAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Version
    @Column(name = "version")
    private Long version;

    /**
     * OPEN → IN_PROGRESS.
     * Assume o atendimento da pendencia.
     */
    public void startProgress() {
        if (this.status != IssueStatus.OPEN) {
            throw new InvalidIssueTransitionException(this.status, IssueStatus.IN_PROGRESS);
        }
        this.status = IssueStatus.IN_PROGRESS;
    }

    /**
     * IN_PROGRESS → RESOLVED.
     * Marca a pendencia como resolvida apos acao corretiva.
     */
    public void resolve() {
        if (this.status != IssueStatus.IN_PROGRESS) {
            throw new InvalidIssueTransitionException(this.status, IssueStatus.RESOLVED);
        }
        this.status = IssueStatus.RESOLVED;
        this.resolvedAt = Instant.now();
    }

    /**
     * RESOLVED → VALIDATED.
     * SENAI confirma que a resolucao esta adequada.
     */
    public void validate() {
        if (this.status != IssueStatus.RESOLVED) {
            throw new InvalidIssueTransitionException(this.status, IssueStatus.VALIDATED);
        }
        this.status = IssueStatus.VALIDATED;
    }

    /**
     * RESOLVED → REOPENED.
     * SENAI rejeita a resolucao e retorna a pendencia para retrabalho.
     */
    public void reopen() {
        if (this.status != IssueStatus.RESOLVED) {
            throw new InvalidIssueTransitionException(this.status, IssueStatus.REOPENED);
        }
        this.status = IssueStatus.REOPENED;
        this.resolvedAt = null;
    }

    /**
     * REOPENED → IN_PROGRESS.
     * Retoma o atendimento apos reabertura.
     */
    public void restartProgress() {
        if (this.status != IssueStatus.REOPENED) {
            throw new InvalidIssueTransitionException(this.status, IssueStatus.IN_PROGRESS);
        }
        this.status = IssueStatus.IN_PROGRESS;
        this.resolvedAt = null;
    }

    /**
     * OPEN ou IN_PROGRESS → CANCELED.
     * Descarta a pendencia.
     */
    public void cancel() {
        if (this.status != IssueStatus.OPEN && this.status != IssueStatus.IN_PROGRESS) {
            throw new InvalidIssueTransitionException(this.status, IssueStatus.CANCELED);
        }
        this.status = IssueStatus.CANCELED;
    }
}

