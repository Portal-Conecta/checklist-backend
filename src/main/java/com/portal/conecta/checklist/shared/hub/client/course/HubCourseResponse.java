package com.portal.conecta.checklist.shared.hub.client.course;

import com.portal.conecta.checklist.module.checklist.domain.valueobject.CourseReference;

import java.util.UUID;

public record HubCourseResponse(UUID id, String name, String code) {

    public CourseReference toReference(UUID requestedCourseId) {
        UUID referenceId = id == null ? requestedCourseId : id;
        return new CourseReference(referenceId, name, code);
    }
}
