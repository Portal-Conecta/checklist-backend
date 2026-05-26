package com.portal.conecta.checklist.shared.hub.mock;

import com.portal.conecta.checklist.shared.hub.properties.HubMockProperties;
import com.portal.conecta.checklist.shared.hub.provider.HubClassProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Profile("mock")
public class MockHubClassProvider implements HubClassProvider {

    private final Set<UUID> ids;

    public MockHubClassProvider(HubMockProperties props){
        this.ids = props.classIds().stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
    }
    @Override
    public boolean existById(UUID classId){
        return ids.contains(classId);
    }

}
