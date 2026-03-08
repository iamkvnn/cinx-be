package com.cinx.cart.config;

import com.cinx.common.utils.AuthenticationUtil;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class FeignConfig {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            String token = "Bearer " + AuthenticationUtil.extractJwt();
            requestTemplate.header("Authorization", token);
        };
    }
}
