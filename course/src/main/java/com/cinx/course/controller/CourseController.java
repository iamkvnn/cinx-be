package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.course.consts.CoursePublishStatus;
import com.cinx.course.consts.CourseStatus;
import com.cinx.course.dto.request.CreateCourseRequest;
import com.cinx.course.dto.request.MoveLessonRequest;
import com.cinx.course.dto.request.MoveSectionRequest;
import com.cinx.course.dto.request.UpdateCourseRequest;
import com.cinx.course.dto.response.*;
import com.cinx.course.service.course.ICourseService;
import com.cinx.course.service.curriculum.ICurriculumService;
import com.cinx.course.service.lesson.ILessonService;
import com.cinx.course.service.section.ISectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {
    private final ICourseService courseService;
    private final ICurriculumService curriculumService;
    private final ISectionService sectionService;
    private final ILessonService lessonService;

    @GetMapping
    public ResponseEntity<PaginatedApiResponse<CourseResponse>> getAllCourses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Integer priceFrom,
            @RequestParam(required = false) Integer priceTo,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String instructorId
    ) {
        Page<CourseResponse> courses = courseService.getAllPublishedCourses(
                query,
                categoryId,
                instructorId,
                rating,
                priceFrom,
                priceTo,
                page,
                size,
                sort);
        return ResponseEntity.ok(PaginationWrapper.wrap(courses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> getReadableCourseById(@PathVariable("id") String courseId) {
        String currentUserId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Course fetched successfully", courseService.getReadableCourseById(currentUserId, courseId))
        );
    }

    @Operation(summary = "Get my courses", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/mine")
    public ResponseEntity<PaginatedApiResponse<CourseResponse>> getMyCourses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Integer priceFrom,
            @RequestParam(required = false) Integer priceTo,
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(required = false) CoursePublishStatus publishStatus,
            @RequestParam(required = false) String categoryId
    ) {
        Page<CourseResponse> courses = courseService.getAllCourses(
                query,
                categoryId,
                AuthenticationUtil.extractUserId(),
                rating,
                priceFrom,
                priceTo,
                status,
                publishStatus,
                page,
                size,
                sort);
        return ResponseEntity.ok(PaginationWrapper.wrap(courses));
    }

    @Operation(summary = "Get editable course draft", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/{id}/draft")
    public ResponseEntity<ApiResponse<CourseResponse>> getEditableCourseDraft(@PathVariable("id") String courseId) {
        String currentUserId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Course draft fetched successfully",
                courseService.getEditableDraftCourseById(currentUserId, courseId)
        ));
    }

    @GetMapping("/{id}/curriculum")
    public ResponseEntity<ApiResponse<CourseCurriculumResponse>> getReadableCurriculum(@PathVariable("id") String courseId) {
        String currentUserId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Course curriculum fetched successfully",
                curriculumService.getReadableCurriculum(currentUserId, courseId)
        ));
    }

    @Operation(summary = "Get editable course curriculum", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/{id}/draft/curriculum")
    public ResponseEntity<ApiResponse<CourseCurriculumResponse>> getEditableDraftCurriculum(@PathVariable("id") String courseId) {
        String currentUserId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Course draft curriculum fetched successfully",
                curriculumService.getEditableDraftCurriculum(currentUserId, courseId)
        ));
    }

    @Operation(summary = "Move editable course section", security = @SecurityRequirement(name = "bearer-jwt"))
    @PatchMapping("/{id}/curriculum/sections/{sectionId}/position")
    public ResponseEntity<ApiResponse<SectionPositionResponse>> moveSection(
            @PathVariable String id,
            @PathVariable String sectionId,
            @Valid @RequestBody MoveSectionRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Section position updated successfully",
                sectionService.moveSection(id, sectionId, request)
        ));
    }

    @Operation(summary = "Move editable course lesson", security = @SecurityRequirement(name = "bearer-jwt"))
    @PatchMapping("/{id}/curriculum/lessons/{lessonId}/position")
    public ResponseEntity<ApiResponse<LessonPositionResponse>> moveLesson(
            @PathVariable String id,
            @PathVariable String lessonId,
            @Valid @RequestBody MoveLessonRequest request
    ) {
        String currentUserId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Lesson position updated successfully",
                lessonService.moveLesson(currentUserId, id, lessonId, request)
        ));
    }

    @GetMapping("/ids")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getCoursesByIds(@RequestParam("ids") List<String> courseIds) {
        String currentUserId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Course fetched successfully", courseService.getReadableCourseByIds(currentUserId, courseIds))
        );
    }

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        String currentUserId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Course created successfully", courseService.createCourse(currentUserId, request))
        );
    }

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(@PathVariable("id") String courseId, @Valid @RequestBody UpdateCourseRequest request) {
        String currentUserId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Course updated successfully", courseService.updateCourse(currentUserId, courseId, request))
        );
    }

    @Operation(summary = "Submit course for approval", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> submitCourse(@PathVariable("id") String courseId) {
        String currentUserId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Course submitted successfully", courseService.submitCourse(currentUserId, courseId))
        );
    }

    @Operation(summary = "Archive course", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> archiveCourse(@PathVariable("id") String courseId) {
        String currentUserId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Course archived successfully",
                courseService.archiveCourse(currentUserId, courseId)
        ));
    }

    @Operation(summary = "Unarchive course", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/{id}/unarchive")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> unarchiveCourse(@PathVariable("id") String courseId) {
        String currentUserId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Course unarchived successfully",
                courseService.unarchiveCourse(currentUserId, courseId)
        ));
    }

    @GetMapping("/{id}/reject-reason")
    public ResponseEntity<ApiResponse<RejectCourseResponse>> getRejectReason(@PathVariable("id") String courseId) {
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Reject reason fetched successfully", courseService.getRejectReason(courseId))
        );
    }
}
