package com.cinx.user.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.user.dto.CreateUserRequest;
import com.cinx.user.dto.UpdateProifileRequest;
import com.cinx.user.dto.UserDto;
import com.cinx.user.service.user.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(Principal principal) {
        UserDto user = userService.findByUserId(principal.getName());
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Current user fetched successfully", user)
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createUser(@RequestBody CreateUserRequest registerDto) {
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "User created successfully", userService.createUser(registerDto))
        );
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //@PreAuthorize("#id == authentication.name or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> updateUser(
            @PathVariable("id") String id,
            @Valid @RequestPart("user") UpdateProifileRequest dto,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar
    ) {
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "User updated successfully", userService.updateProfile(id, dto, avatar))
        );
    }

    @GetMapping("/avatars/{fileName:.+}")
    public ResponseEntity<Resource> getBannerImage(@PathVariable String fileName) throws IOException {
        Path imagePath = Paths.get("uploads/avatars/").resolve(fileName).normalize();
        if (!Files.exists(imagePath)) {
            return ResponseEntity.notFound().build();
        }
        String contentType = Files.probeContentType(imagePath);
        Resource resource = new UrlResource(imagePath.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
