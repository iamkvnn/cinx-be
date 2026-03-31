package com.cinx.notification;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@OpenAPIDefinition(
                servers = {
                                @Server(url = "${gateway.url}", description = "API Gateway")
                }
)
@SpringBootApplication(scanBasePackages = {"com.cinx.notification", "com.cinx.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.cinx")public class NotificationApplication {
	public static void main(String[] args) {
		SpringApplication.run(NotificationApplication.class, args);
	}

}
