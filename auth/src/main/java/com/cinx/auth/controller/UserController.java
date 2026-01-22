package com.cinx.auth.controller;

import com.cinx.auth.dto.ApiResponse;
import com.cinx.auth.dto.UserDto;
import com.cinx.auth.model.User;
import com.cinx.auth.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getCurrentUser(Principal principal) {
        User user = userService.findByEmail(principal.getName());
        return ResponseEntity.ok().body(
                new ApiResponse(true, "Current user fetched successfully",
                        new UserDto(user.getId(), user.getName(), user.getName(), user.getRole(), user.getGender())
                        )
        );
    }
}
