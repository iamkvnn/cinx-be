package com.cinx.course.service.instructor;

import com.cinx.course.dto.request.CreateInstructorRequest;
import com.cinx.course.dto.request.UpdateInstructorRequest;
import com.cinx.course.dto.response.InstructorResponse;
import org.springframework.data.domain.Page;

public interface IInstructorService {
    void createInstructor(CreateInstructorRequest request);
    void updateInstructor(String id, UpdateInstructorRequest request);
    void deleteInstructor(String id);
    InstructorResponse getInstructorById(String id);
    Page<InstructorResponse> getAllInstructors(int page, int size);
}
