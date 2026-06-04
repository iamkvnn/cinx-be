package com.cinx.course.config;

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
            requestTemplate.header("Authorization", "Bearer " + jwt);
        };
    }
}
