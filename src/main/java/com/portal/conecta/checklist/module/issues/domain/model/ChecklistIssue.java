package com.portal.conecta.checklist.module.issues.domain.model;

import com.portal.conecta.checklist.module.checklist.domain.valueobject.UserReference;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

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

    @Column(name = "item_title_snapshot",nullable = false,length = 150)
    private  String itemTitleSnapshot;

    @Column(name = "title",nullable = false,length = 100)
    private String title;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @Column(name = "due_at",nullable = false)
    private Instant dueAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    public void resolve() {
        this.status = Status.RESOLVED;
        this.resolvedAt = Instant.now();
    }
}
