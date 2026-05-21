package com.portal.conecta.checklist.shared.hub.dto;

import java.util.UUID;

public record UserDTO(
        UUID id,
        String name,
        String email,
        String avatarUrl,
        String typeUser
) {}
