package com.portal.conecta.checklist.shared.hub.mock.classes;

import com.portal.conecta.checklist.shared.hub.properties.HubMockProperties;
import com.portal.conecta.checklist.shared.hub.provider.classes.HubClassInfo;
import com.portal.conecta.checklist.shared.hub.provider.classes.HubClassProvider;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Profile({"mock", "test"})
public class MockHubClassProvider implements HubClassProvider {

    private final Set<UUID> ids;
    private final List<UUID> orderedIds;

    public MockHubClassProvider(HubMockProperties props) {
        this.orderedIds = props.classIds().stream()
                .map(UUID::fromString)
                .collect(Collectors.toList());
        this.ids = Set.copyOf(orderedIds);
    }

    @Override
    public boolean existsById(UUID classId) {
        return ids.contains(classId);
    }

    @Override
    public HubClassInfo findById(UUID classId) {
        if (!ids.contains(classId)) {
            throw new EntityNotFoundException("Turma nao encontrada no Hub.");
        }
        int index = orderedIds.indexOf(classId) + 1;
        return new HubClassInfo(
                "Turma Mock " + index,
                "Representante1 Mock " + index,
                "Representante2 Mock " + index
        );
    }
}
