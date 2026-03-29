package com.cinx.enrollment;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@OpenAPIDefinition(
		servers = {
				@Server(url = "http://localhost:9090", description = "API Gateway")
		}
)
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.cinx"})
@SpringBootApplication(scanBasePackages = {"com.cinx.enrollment", "com.cinx.common"})
public class EnrollmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnrollmentApplication.class, args);
	}

}
