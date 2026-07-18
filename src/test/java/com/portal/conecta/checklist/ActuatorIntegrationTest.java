package com.portal.conecta.checklist;

import com.portal.conecta.logging.AccessLogFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(properties = {
		"checklist.security.jwt.secret=dGVzdC1vbmx5LWp3dC1zZWNyZXQtMzItYnl0ZXMtbm90LXJlYWw=",
		"hub.api.url=http://localhost:8080",
		"spring.datasource.url=jdbc:h2:mem:actuator-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;INIT=CREATE DOMAIN IF NOT EXISTS JSONB AS JSON",
		"spring.jpa.hibernate.ddl-auto=none",
		"spring.flyway.enabled=false",
		"management.prometheus.metrics.export.enabled=true"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ActuatorIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	void shouldGetHealthEndpointWithDetailsAndStatusUp() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"));
	}

	@Test
	void shouldGetInfoEndpointWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/actuator/info"))
				.andExpect(status().isOk());
	}

	@Test
	void shouldGetPrometheusEndpointWithMetrics() throws Exception {
		mockMvc.perform(get("/actuator/prometheus"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("jvm_memory_used_bytes")))
				.andExpect(content().string(containsString("process_cpu_usage")));
	}

	@Test
	void shouldUseOnlySharedAccessLogFilter() {
		assertThat(applicationContext.getBeansOfType(AccessLogFilter.class)).hasSize(1);
	}
}
