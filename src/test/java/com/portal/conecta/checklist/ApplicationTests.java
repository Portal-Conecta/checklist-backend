package com.portal.conecta.checklist;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
		"checklist.security.jwt.secret=dGVzdC1vbmx5LWp3dC1zZWNyZXQtMzItYnl0ZXMtbm90LXJlYWw=",
		"hub.api.url=http://localhost:8080"
})
@ActiveProfiles("test")
class ApplicationTests {

	@Test
	void contextLoads() {
	}

}
