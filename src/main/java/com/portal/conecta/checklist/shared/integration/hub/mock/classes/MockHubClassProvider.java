package com.portal.conecta.checklist.shared.integration.hub.mock.classes;

import com.portal.conecta.checklist.modules.checklist.domain.enums.Shift;
import com.portal.conecta.checklist.modules.checklist.domain.valueobject.ClassReference;
import com.portal.conecta.checklist.shared.integration.hub.config.HubMockProperties;
import com.portal.conecta.checklist.modules.checklist.application.port.out.integration.HubClassProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Provider mockado de turmas do Hub.
 *
 * <p>Valida existencia de turmas a partir das propriedades locais do profile
 * {@code mock} ou {@code test}.</p>
 */
@Component
@Profile({"mock", "test"})
public class MockHubClassProvider implements HubClassProvider {

    private final Set<UUID> ids;
    private final Map<UUID, Shift> shiftsByClassId;

    public MockHubClassProvider(HubMockProperties props) {
        this.ids = props.classIds().stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
        this.shiftsByClassId = props.classShifts().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> UUID.fromString(entry.getKey()),
                        entry -> Shift.valueOf(entry.getValue())
                ));
    }

    @Override
    public boolean existsById(UUID classId) {
        return ids.contains(classId);
    }

    @Override
    public Optional<ClassReference> findById(UUID classId) {
        return existsById(classId)
                ? Optional.of(new ClassReference(classId, null, null, shiftsByClassId.get(classId), null, null))
                : Optional.empty();
    }
}
