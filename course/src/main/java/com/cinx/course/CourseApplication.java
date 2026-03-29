package com.cinx.course;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@OpenAPIDefinition(
		servers = {
				@Server(url = "http://localhost:9090", description = "API Gateway")
		}
)
@SpringBootApplication(scanBasePackages = {"com.cinx.course", "com.cinx.common"})
@EnableDiscoveryClient
public class CourseApplication {

	public static void main(String[] args) {
		SpringApplication.run(CourseApplication.class, args);
	}

}
