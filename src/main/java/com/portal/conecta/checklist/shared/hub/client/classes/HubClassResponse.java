package com.portal.conecta.checklist.shared.hub.client.classes;

import com.portal.conecta.checklist.module.checklist.domain.enums.Shift;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.ClassReference;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.CourseReference;

import java.time.Instant;
import java.util.UUID;

public record HubClassResponse(
        UUID id,
        Shift shift,
        Integer number,
        String name,
        UUID courseId,
        Instant createdAt
) {

    public ClassReference toReference(UUID requestedClassId) {
        UUID referenceId = id == null ? requestedClassId : id;
        CourseReference courseReference = courseId == null ? null : new CourseReference(courseId);
        return new ClassReference(referenceId, name, number, shift, courseReference, createdAt);
    }
}
