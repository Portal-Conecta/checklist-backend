package com.portal.conecta.checklist.modules.checklist.application.port.out.integration;

import com.portal.conecta.checklist.modules.checklist.domain.valueobject.CourseReference;

import java.util.Optional;
import java.util.UUID;

public interface HubCourseProvider {

    boolean existsById(UUID courseId);

    default Optional<CourseReference> findById(UUID courseId) {
        return existsById(courseId)
                ? Optional.of(new CourseReference(courseId))
                : Optional.empty();
    }
}
