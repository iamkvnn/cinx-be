package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.course.dto.request.CreateSectionRequest;
import com.cinx.course.dto.request.UpdateSectionRequest;
import com.cinx.course.dto.response.SectionResponse;
import com.cinx.course.mapper.SectionMapper;
import com.cinx.course.service.section.ISectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses/{courseId}/sections")
@RequiredArgsConstructor
public class SectionController {
    private final ISectionService sectionService;
    private final SectionMapper sectionMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SectionResponse>>> getSectionsByCourseId(@PathVariable String courseId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Success",
                sectionService.getSectionsByCourseId(courseId).stream().map(sectionMapper::toDto).toList()
        ));
    }

    @GetMapping("/{sectionId}")
    public ResponseEntity<ApiResponse<SectionResponse>> getSectionById(
            @PathVariable String courseId,
            @PathVariable String sectionId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Success",
                sectionMapper.toDto(sectionService.getSectionById(courseId, sectionId))
        ));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ApiResponse<SectionResponse>> createSection(
            @PathVariable String courseId,
            @RequestBody CreateSectionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Section created successfully",
                sectionMapper.toDto(sectionService.createSection(courseId, request))
        ));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping("/{sectionId}")
    public ResponseEntity<ApiResponse<SectionResponse>> updateSection(
            @PathVariable String courseId,
            @PathVariable String sectionId,
            @RequestBody UpdateSectionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Section updated successfully",
                sectionMapper.toDto(sectionService.updateSection(courseId, sectionId, request))
        ));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @DeleteMapping("/{sectionId}")
    public ResponseEntity<ApiResponse<Void>> deleteSection(
            @PathVariable String courseId,
            @PathVariable String sectionId
    ) {
        sectionService.deleteSection(courseId, sectionId);
        return ResponseEntity.ok(ApiResponse.success("Section deleted successfully", null));
    }
}
