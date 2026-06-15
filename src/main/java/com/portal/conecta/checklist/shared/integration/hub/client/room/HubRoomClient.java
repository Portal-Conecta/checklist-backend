package com.portal.conecta.checklist.shared.integration.hub.client.room;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "hub-room-client", url = "${hub.api.url}")
public interface HubRoomClient {

    @GetMapping("/rooms/{id}")
    HubRoomResponse findById(@PathVariable("id") UUID roomId);

    @PostMapping("/rooms/bulk")
    HubBulkRoomResponse findBulk(@RequestBody HubBulkRoomRequest request);
}
