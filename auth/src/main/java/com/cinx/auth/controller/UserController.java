package com.cinx.auth.controller;

import com.cinx.auth.dto.ApiResponse;
import com.cinx.auth.dto.UpdateProifileDto;
import com.cinx.auth.dto.UserDto;
import com.cinx.auth.model.User;
import com.cinx.auth.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getCurrentUser(Principal principal) {
        User user = userService.findById(principal.getName());
        return ResponseEntity.ok().body(
                new ApiResponse(true, "Current user fetched successfully",
                        new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getGender())
                        )
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("#id == principal.username")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable("id") String id, @RequestBody UpdateProifileDto dto) {
        User user = userService.updateUser(id, User.builder().name(dto.name()).gender(dto.gender()).build());
        return ResponseEntity.ok().body(
                new ApiResponse(true, "User updated successfully",
                        new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getGender())
                )
        );
    }
}
