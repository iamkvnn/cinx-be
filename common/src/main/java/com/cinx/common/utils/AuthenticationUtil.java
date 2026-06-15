package com.cinx.common.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public class AuthenticationUtil {

    public static String extractUserId() {
        Authentication authentication = getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getName() != null
                && !"anonymousUser".equals(authentication.getName())) {
            return authentication.getName();
        }
        return null;
    }

    public static String extractJwt() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            return null;
        }
        return ((Jwt) authentication.getPrincipal()).getTokenValue();
    }

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
