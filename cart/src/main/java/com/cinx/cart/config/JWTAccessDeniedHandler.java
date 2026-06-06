package com.cinx.cart.config;

import com.cinx.common.exception.ErrorCode;
import com.cinx.common.exception.ProblemDetailResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JWTAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        ProblemDetailResponseWriter.write(request, response, HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN, "Access denied");
    }
}
