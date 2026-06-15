package com.portal.conecta.checklist.shared.integration.hub.client.course;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "hub-course-client", url = "${hub.api.url}")
public interface HubCourseClient {

    @GetMapping("/courses/{courseId}")
    HubCourseResponse findById(@PathVariable("courseId") UUID courseId);
}
