package com.portal.conecta.checklist.shared.hub.client.me;

import com.portal.conecta.checklist.shared.hub.client.user.HubMyListCourseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;

@Profile("!mock & !test")
@FeignClient(name = "hub-me-client", url = "${hub.api.url}")
public interface HubMeClient {

    @GetMapping("/me/courses")
    HubMyListCourseResponse findMyCourses();
}
