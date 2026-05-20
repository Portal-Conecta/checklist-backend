package com.portal.conecta.checklist.module.checklist.domain.model;

import com.portal.conecta.checklist.module.checklist.domain.valueobject.RoomReference;
import com.portal.conecta.checklist.module.checklist.presentation.dto.UserToken;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "checklist_template")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Embedded
    private RoomReference roomReference;

    @Column(name = "title", nullable = false , length = 150)
    private String title;

    @Column(name = "description", nullable = false, length = 250)
    private String description;

    @Column(name = "version",nullable = false)
    private int version;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.DRAFT;

    @Column(name = "active",nullable = false)
    private boolean active;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "schema_json", columnDefinition = "jsonb", nullable = false)
    private UserToken schemaJson;



}
