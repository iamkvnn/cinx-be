package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.course.dto.request.CreateInstructorRequest;
import com.cinx.course.dto.request.UpdateInstructorRequest;
import com.cinx.course.service.instructor.IInstructorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/instructors")
public class InstructorController {
    private final IInstructorService instructorService;

    @GetMapping
    public ResponseEntity<PaginatedApiResponse<?>> getAllInstructors(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(PaginationWrapper.wrap(instructorService.getAllInstructors(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getInstructorById(@PathVariable String id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "", instructorService.getInstructorById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createInstructor(@RequestBody CreateInstructorRequest request) {
        instructorService.createInstructor(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Instructor created successfully", null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateInstructor(@PathVariable String id, @RequestBody UpdateInstructorRequest request) {
        instructorService.updateInstructor(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Instructor updated successfully", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteInstructor(@PathVariable String id) {
        instructorService.deleteInstructor(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Instructor deleted successfully", null));
    }
}
