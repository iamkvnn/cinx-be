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
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(@PathVariable("id") String courseId) {
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Course fetched successfully", courseService.getPublishedCourseById(courseId))
        );
    }

    @Operation(summary = "Get enrolled course", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/{id}/enrolled")
    public ResponseEntity<ApiResponse<CourseResponse>> getEnrolledCourseById(@PathVariable("id") String courseId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Course fetched successfully",
                courseService.getEnrolledCourseByIdForCurrentUser(courseId)
        ));
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
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseDraft(@PathVariable("id") String courseId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Course draft fetched successfully",
                courseService.getOwnedDraftCourseById(courseId)
        ));
    }

    @Operation(summary = "Get owned published course snapshot", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/{id}/published")
    public ResponseEntity<ApiResponse<CourseResponse>> getPublishedSnapshot(@PathVariable("id") String courseId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Course published snapshot fetched successfully",
                courseService.getOwnedPublishedSnapshotCourseById(courseId)
        ));
    }

    @GetMapping("/{id}/curriculum")
    public ResponseEntity<ApiResponse<CourseCurriculumResponse>> getPublishedCurriculum(@PathVariable("id") String courseId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Course curriculum fetched successfully",
                curriculumService.getPublishedCurriculum(courseId)
        ));
    }

    @Operation(summary = "Get enrolled course curriculum", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/{id}/enrolled/curriculum")
    public ResponseEntity<ApiResponse<CourseCurriculumResponse>> getEnrolledCurriculum(@PathVariable("id") String courseId) {
        courseService.getEnrolledCourseByIdForCurrentUser(courseId);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Course curriculum fetched successfully",
                curriculumService.getEnrolledCurriculum(courseId)
        ));
    }

    @Operation(summary = "Get editable course curriculum", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/{id}/draft/curriculum")
    public ResponseEntity<ApiResponse<CourseCurriculumResponse>> getDraftCurriculum(@PathVariable("id") String courseId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Course draft curriculum fetched successfully",
                curriculumService.getOwnedDraftCurriculum(courseId)
        ));
    }

    @Operation(summary = "Get owned published course curriculum snapshot", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/{id}/published/curriculum")
    public ResponseEntity<ApiResponse<CourseCurriculumResponse>> getPublishedSnapshotCurriculum(@PathVariable("id") String courseId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Course published curriculum snapshot fetched successfully",
                curriculumService.getOwnedPublishedSnapshotCurriculum(courseId)
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
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Lesson position updated successfully",
                lessonService.moveLesson(id, lessonId, request)
        ));
    }

    @GetMapping("/ids")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getCourseById(@RequestParam("ids") List<String> courseIds) {
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Course fetched successfully", courseService.getPublishedCourseByIds(courseIds))
        );
    }

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(@RequestBody CreateCourseRequest request) {
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Course created successfully", courseService.createCourse(request))
        );
    }

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(@PathVariable("id") String courseId, @Valid @RequestBody UpdateCourseRequest request) {
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Course updated successfully", courseService.updateCourse(courseId, request))
        );
    }

    @Operation(summary = "Submit course for approval", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<CourseResponse>> submitCourse(@PathVariable("id") String courseId) {
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Course submitted successfully", courseService.submitCourse(courseId))
        );
    }

    @Operation(summary = "Archive course", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<CourseResponse>> archiveCourse(@PathVariable("id") String courseId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Course archived successfully",
                courseService.archiveCourse(courseId)
        ));
    }

    @Operation(summary = "Unarchive course", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/{id}/unarchive")
    public ResponseEntity<ApiResponse<CourseResponse>> unarchiveCourse(@PathVariable("id") String courseId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Course unarchived successfully",
                courseService.unarchiveCourse(courseId)
        ));
    }

    @GetMapping("/{id}/reject-reason")
    public ResponseEntity<ApiResponse<RejectCourseResponse>> getRejectReason(@PathVariable("id") String courseId) {
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Reject reason fetched successfully", courseService.getRejectReason(courseId))
        );
    }
}
