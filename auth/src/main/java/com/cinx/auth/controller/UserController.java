package com.cinx.auth.controller;

import com.cinx.auth.dto.ApiResponse;
import com.cinx.auth.dto.UpdateProifileDto;
import com.cinx.auth.dto.UserDto;
import com.cinx.auth.model.User;
import com.cinx.auth.service.user.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<ApiResponse> getCurrentUser(Principal principal) {
        User user = userService.findById(principal.getName());
        return ResponseEntity.ok().body(
                new ApiResponse(true, "Current user fetched successfully",
                        new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getGender(), user.getAvatarUrl())
                        )
        );
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("#id == authentication.name or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable("id") String id, @Valid @RequestPart("user") UpdateProifileDto dto, @RequestPart(value = "avatar", required = false) MultipartFile avatar) {
        System.out.println("Updating user with ID: " + id);
        User user = userService.updateProfile(id, dto, avatar);
        return ResponseEntity.ok().body(
                new ApiResponse(true, "User updated successfully",
                        new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getGender(), user.getAvatarUrl())
                )
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
