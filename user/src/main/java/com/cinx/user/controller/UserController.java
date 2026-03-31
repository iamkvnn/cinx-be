package com.cinx.user.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.user.dto.CreateUserRequest;
import com.cinx.user.dto.UpdateProfileRequest;
import com.cinx.user.dto.UserDto;
import com.cinx.user.dto.request.DeviceTokenRequest;
import com.cinx.user.service.user.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping
    public ResponseEntity<PaginatedApiResponse<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok().body(
                PaginationWrapper.wrap(userService.findAll(page, size))
        );
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(Principal principal) {
        UserDto user = userService.findByUserId(principal.getName());
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Current user fetched successfully", user)
        );
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/ids")
    public ResponseEntity<ApiResponse<List<UserDto>>> getUsersByIds(@RequestParam("ids") List<String> ids) {
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Users fetched successfully", userService.findByIds(ids))
        );
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable String id) {
        UserDto user = userService.findByUserId(id);
        System.out.println("User: " + user);
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "User fetched successfully", user)
        );
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/{id}/instructor-verified")
    public ResponseEntity<ApiResponse<Boolean>> checkInstructorVerified(@PathVariable String id) {
        boolean isVerified = userService.checkInstructorVerified(id);
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Instructor verification status fetched successfully", isVerified)
        );
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ApiResponse<UserDto>> createUser(@RequestBody CreateUserRequest registerDto) {
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "User created successfully", userService.createUser(registerDto))
        );
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping(value = "/{id}/verify-instructor")
    public ResponseEntity<ApiResponse<?>> verifyInstructor(@PathVariable String id) {
        userService.verifyInstructor(id);
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Instructor verified successfully", null)
        );
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //@PreAuthorize("#id == authentication.name or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable("id") String id,
            @Valid @RequestPart("user") UpdateProfileRequest dto,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar
    ) {
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "User updated successfully", userService.updateProfile(id, dto, avatar))
        );
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/avatars/{fileName:.+}")
    public ResponseEntity<Resource> getAvatarImage(@PathVariable String fileName) throws IOException {
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

    @Operation(summary = "Save user FCM device token", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/device-tokens")
    public ApiResponse<Void> saveDeviceToken(@Valid @RequestBody DeviceTokenRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        userService.saveDeviceToken(userId, request);
        return new ApiResponse<>(true, "Device token saved successfully", null);
    }

    @Operation(summary = "Get user FCM device tokens (internal)", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/{userId}/fcm-tokens")
    public ApiResponse<List<String>> getUserTokens(@PathVariable String userId) {
        return new ApiResponse<>(true, "Success", userService.getUserTokens(userId));
    }

    @Operation(summary = "Add XP to user profile (internal)", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/{userId}/add-xp")
    public ApiResponse<UserDto> addXp(@PathVariable String userId, @RequestParam Integer amount) {
        return new ApiResponse<>(true, "XP added successfully", userService.addXp(userId, amount));
    }
}
