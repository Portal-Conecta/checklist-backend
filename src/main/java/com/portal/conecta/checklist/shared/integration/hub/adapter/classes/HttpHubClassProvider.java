package com.portal.conecta.checklist.shared.integration.hub.adapter.classes;

import com.portal.conecta.checklist.modules.checklist.application.port.out.integration.HubClassProvider;
import com.portal.conecta.checklist.modules.checklist.domain.valueobject.ClassReference;
import com.portal.conecta.checklist.modules.checklist.domain.valueobject.CourseReference;
import com.portal.conecta.checklist.shared.integration.hub.client.classes.HubClassClient;
import com.portal.conecta.checklist.shared.integration.hub.client.classes.HubClassResponse;
import com.portal.conecta.checklist.shared.integration.hub.exception.HubIntegrationException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Provider HTTP para consultar turmas no Hub real.
 *
 * <p>Converte respostas de nao encontrado em {@code false} e falhas de
 * comunicacao em excecoes de integracao.</p>
 */
@Component
@RequiredArgsConstructor
public class HttpHubClassProvider implements HubClassProvider {

    private final HubClassClient hubClassClient;

    @Override
    public boolean existsById(UUID classId) {
        return findById(classId).isPresent();
    }

    @Override
    public Optional<ClassReference> findById(UUID classId) {
        try {
            HubClassResponse response = hubClassClient.findById(classId);

            return response == null ? Optional.empty() : Optional.of(toReference(response, classId));
        } catch (FeignException.NotFound exception) {
            return Optional.empty();
        } catch (FeignException exception) {
            throw new HubIntegrationException("Servico de turmas do Hub indisponivel.", exception);
        }
    }

    private ClassReference toReference(HubClassResponse response, UUID requestedClassId) {
        UUID classId = response.id() == null ? requestedClassId : response.id();
        CourseReference courseReference = response.courseId() == null ? null : new CourseReference(response.courseId());

        return new ClassReference(
                classId,
                response.name(),
                response.number(),
                response.shift(),
                courseReference,
                response.createdAt()
        );
    }
}
