package com.portal.conecta.checklist.shared.integration.hub.client.classes;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "hub-class-client", url = "${hub.api.url}")
public interface HubClassClient {

    @GetMapping("/classes/{classId}")
    HubClassResponse findById(@PathVariable("classId") UUID classId);

    @PostMapping("/classes/bulk")
    HubBulkClassResponse findBulk(@RequestBody HubBulkClassRequest request);
}
