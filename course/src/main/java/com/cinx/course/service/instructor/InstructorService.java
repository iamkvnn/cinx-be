package com.cinx.course.service.instructor;

import com.cinx.course.dto.request.CreateInstructorRequest;
import com.cinx.course.dto.request.UpdateInstructorRequest;
import com.cinx.course.dto.response.InstructorResponse;
import com.cinx.course.mapper.InstructorMapper;
import com.cinx.course.model.Instructor;
import com.cinx.course.repository.InstructorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InstructorService implements IInstructorService {
    private final InstructorRepository instructorRepository;
    private final InstructorMapper instructorMapper;

    @Override
    public void createInstructor(CreateInstructorRequest request) {
        instructorRepository.save(instructorMapper.toModel(request));
    }

    @Override
    public void updateInstructor(String id, UpdateInstructorRequest request) {
            Instructor instructor = instructorRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Instructor not found"));
            instructorMapper.partialUpdate(instructor, request);
            instructorRepository.save(instructor);
    }

    @Override
    public void deleteInstructor(String id) {
        instructorRepository.deleteById(id);
    }

    @Override
    public InstructorResponse getInstructorById(String id) {
        return instructorRepository.findById(id)
                .map(instructorMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
    }

    @Override
    public Page<InstructorResponse> getAllInstructors(int page, int size) {
        return instructorRepository.findAll(PageRequest.of(page - 1, size))
                .map(instructorMapper::toDto);
    }
}
