package com.portal.conecta.checklist.shared.hub.mock.course;

import com.portal.conecta.checklist.shared.hub.properties.HubMockProperties;
import com.portal.conecta.checklist.shared.hub.provider.course.HubCourseProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Provider mockado de cursos do Hub.
 *
 * <p>Permite validar integracoes de curso em ambiente local e testes sem
 * depender do Hub real.</p>
 */
@Component
@Profile({"mock", "test"})
public class MockHubCourseProvider implements HubCourseProvider {

    private final Set<UUID> ids;

    public MockHubCourseProvider(HubMockProperties props) {
        this.ids = props.courseIds().stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean existsById(UUID courseId) {
        return ids.contains(courseId);
    }
}
