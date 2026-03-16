package com.cinx.social;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;

@EnableFeignClients(basePackages = "com.cinx")
@SpringBootApplication(scanBasePackages = {"com.cinx.social", "com.cinx.common"})
public class SocialApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialApplication.class, args);
	}

}
