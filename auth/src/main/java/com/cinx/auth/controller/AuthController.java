package com.cinx.auth.controller;

import com.cinx.auth.dto.ApiResponse;
import com.cinx.auth.dto.AuthRequestDto;
import com.cinx.auth.dto.RegisterDto;
import com.cinx.auth.dto.UserDto;
import com.cinx.auth.model.User;
import com.cinx.auth.service.user.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IUserService userService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterDto registerDto) {
        User user = userService.createUser(registerDto);
        return ResponseEntity.ok(
                new ApiResponse(true, "User registered successfully", new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getGender()))
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(
            @RequestBody AuthRequestDto request,
            HttpServletRequest httpRequest
    ) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                );
        Authentication auth = authenticationManager.authenticate(authToken);
        User user = userService.findByEmail(request.email());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        httpRequest.getSession(true)
                .setAttribute(
                        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        context
                );
        return ResponseEntity.ok(
                new ApiResponse(true, "User logged in successfully", new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getGender()))
        );
    }

}
