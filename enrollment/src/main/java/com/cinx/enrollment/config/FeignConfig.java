package com.cinx.enrollment.config;

import com.cinx.common.utils.AuthenticationUtil;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class FeignConfig {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            String jwt = AuthenticationUtil.extractJwt();
            if (jwt == null) {
                return;
            }
            String token = "Bearer " + jwt;
            requestTemplate.header("Authorization", token);
        };
    }
}
