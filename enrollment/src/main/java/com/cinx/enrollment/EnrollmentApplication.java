package com.cinx.enrollment;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@OpenAPIDefinition(
		servers = {
				@Server(url = "${gateway.url}", description = "API Gateway")
		}
)
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.cinx"})
@SpringBootApplication(scanBasePackages = {"com.cinx.enrollment", "com.cinx.common"})
@EnableScheduling
public class EnrollmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnrollmentApplication.class, args);
	}

}
