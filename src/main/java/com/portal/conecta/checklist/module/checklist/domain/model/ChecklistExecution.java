package com.portal.conecta.checklist.module.checklist.domain.model;

import com.portal.conecta.checklist.module.checklist.domain.valueobject.ClassReference;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.RoomReference;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.UserReference;
import com.portal.conecta.checklist.module.issues.domain.model.ChecklistIssue;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "checklist_execution")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checklist_template_id", nullable = false)
    private ChecklistTemplate checklistTemplate;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "class_id", nullable = false)
    private UUID classId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.DRAFT;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "answers_json", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> answersJson;

    @Column(name = "compliance_score", precision = 5, scale = 2)
    private BigDecimal complianceScore;

    @OneToMany(mappedBy = "checklistExecution", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChecklistIssue> issues = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "checklist_type", nullable = false, length = 30)
    private ChecklistType checklistType;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;



    @Column(name = "period", nullable = false, length = 20)
    private Period period;



    public void addIssue(ChecklistIssue issue) {
        issues.add(issue);
        issue.setChecklistExecution(this);
    }
}
