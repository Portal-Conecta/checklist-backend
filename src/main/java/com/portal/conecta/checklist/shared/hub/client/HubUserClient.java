package com.portal.conecta.checklist.shared.hub.client;

import com.portal.conecta.checklist.shared.hub.dto.UserDTO;
import feign.Param;
import feign.RequestLine;

import java.util.UUID;

public interface HubUserClient {

    @RequestLine("GET /api/users/{userId}")
    UserDTO findById(@Param("userId") UUID userId);
}
