package com.cinx.notification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:notification-test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"jwt.access.secret=test-secret-test-secret-test-secret-test-secret",
		"eureka.client.enabled=false",
		"spring.cloud.discovery.enabled=false"
})
class NotificationApplicationTests {

	@Test
	void contextLoads() {
	}

}
