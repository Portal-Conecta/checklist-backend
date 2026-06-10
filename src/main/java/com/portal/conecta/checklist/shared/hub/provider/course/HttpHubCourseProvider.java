package com.portal.conecta.checklist.shared.hub.provider.course;

import com.portal.conecta.checklist.module.checklist.domain.valueobject.CourseReference;
import com.portal.conecta.checklist.shared.hub.client.course.HubCourseClient;
import com.portal.conecta.checklist.shared.hub.client.course.HubCourseResponse;
import com.portal.conecta.checklist.shared.hub.exception.HubIntegrationException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Provider HTTP para consultar cursos no Hub real.
 *
 * <p>Busca os dados reais do curso pelo endpoint {@code /courses/{courseId}}
 * e converte o response para uma referencia de dominio.</p>
 */
@Component
@Profile("!mock & !test")
@RequiredArgsConstructor
public class HttpHubCourseProvider implements HubCourseProvider {

    private final HubCourseClient hubCourseClient;

    @Override
    public boolean existsById(UUID courseId) {
        return findById(courseId).isPresent();
    }

    @Override
    public Optional<CourseReference> findById(UUID courseId) {
        try {
            HubCourseResponse response = hubCourseClient.findById(courseId);

            return response == null ? Optional.empty() : Optional.of(toReference(response, courseId));
        } catch (FeignException.NotFound exception) {
            return Optional.empty();
        } catch (FeignException exception) {
            throw new HubIntegrationException("Servico de cursos do Hub indisponivel.", exception);
        }
    }

    private CourseReference toReference(HubCourseResponse response, UUID requestedCourseId) {
        UUID courseId = response.id() == null ? requestedCourseId : response.id();
        return new CourseReference(courseId, response.name(), response.code());
    }
}
