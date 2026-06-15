package com.cinx.social.config;

import com.cinx.common.utils.AuthenticationUtil;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class FeignConfig {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            String userId = AuthenticationUtil.extractUserId();
            if (userId == null) {
                return;
            }
            requestTemplate.header("X-User-Id", userId);
        };
    }
}
