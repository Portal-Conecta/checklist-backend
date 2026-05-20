package com.portal.conecta.checklist.module.issues.domain.model;

import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "item_key", nullable = false, length = 150)
    private String itemKey;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "resolved", nullable = false)
    private boolean resolved;
}
