package com.portal.conecta.checklist.shared.hub.client.room;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Profile("!mock & !test")
@FeignClient(name = "hub-room-client", url = "${hub.api.url}")
public interface HubRoomClient {

    @GetMapping("/rooms/{roomId}")
    HubRoomResponse findById(@PathVariable("roomId") UUID roomId);
}
