package com.portal.conecta.checklist.module.checklist.domain.model;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.module.checklist.domain.enums.Shift;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(
        name = "checklist_submission_window",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_window_class_type",
                columnNames = {"class_id", "checklist_type"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistSubmissionWindow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "class_id", nullable = false)
    private UUID classId;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift", nullable = false, length = 20)
    private Shift shift;

    @Enumerated(EnumType.STRING)
    @Column(name = "checklist_type", nullable = false, length = 30)
    private ChecklistType checklistType;

    @Column(name = "open_at", nullable = false)
    private LocalTime openAt;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PreUpdate
    public void markUpdatedAt() {
        this.updatedAt = Instant.now();
    }
}
