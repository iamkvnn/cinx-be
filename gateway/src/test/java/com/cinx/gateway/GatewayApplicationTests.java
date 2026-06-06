package com.cinx.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "jwt.access.secret=0123456789012345678901234567890123456789012345678901234567890123")
class GatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}
