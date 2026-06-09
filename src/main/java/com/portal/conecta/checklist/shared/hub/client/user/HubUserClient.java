package com.portal.conecta.checklist.shared.hub.client.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;

@Profile("!mock & !test")
@FeignClient(name = "hub-user-client", url = "${hub.api.url}")
public interface HubUserClient {

    @GetMapping("/me/courses")
    HubMyListCourseResponse findMyCourses();
}
