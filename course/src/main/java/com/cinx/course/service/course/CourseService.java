package com.cinx.course.service.course;

import com.cinx.common.exception.NotFoundException;
import com.cinx.course.dto.request.CreateCourseRequest;
import com.cinx.course.dto.response.CourseDetailResponse;
import com.cinx.course.dto.response.CourseResponse;
import com.cinx.course.mapper.CourseMapper;
import com.cinx.course.model.*;
import com.cinx.course.repository.CategoryRepository;
import com.cinx.course.repository.CourseRepository;
import com.cinx.course.repository.InstructorRepository;
import com.cinx.course.service.instructor.IInstructorService;
import com.cinx.course.service.section.ISectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService implements ICourseService {
    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final ISectionService sectionService;
    private final InstructorRepository instructorRepository;
    private final CourseMapper courseMapper;

    @Override
    public CourseDetailResponse getCourseById(String courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        course.setSections(sectionService.getSectionsByCourseId(courseId));
        return courseMapper.toDetailDto(course);
    }

    @Override
    public List<CourseResponse> getCourseByIds(List<String> courseIds) {
        List<Course> courses = courseRepository.findAllById(courseIds);
        return courses.stream().map(courseMapper::toDto).toList();
    }

    @Override
    public Page<CourseResponse> getAllCourses(String query, String categoryId, Pageable pageable) {
        Page<Course> courses = courseRepository.findAll(query, categoryId, pageable);
        return courses.map(courseMapper::toDto);
    }

    @Transactional
    @Override
    public CourseResponse createCourse(CreateCourseRequest request) {
        Course course = courseRepository.save(buildCourseFromRequest(request));
        course.setSections(
                sectionService.createSections(course.getSections().stream()
                        .peek(section -> section.setCourse(course))
                        .toList())
        );
        return courseMapper.toDto(course);
    }

    private Course buildCourseFromRequest(CreateCourseRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + request.categoryId()));
        Instructor instructor = instructorRepository.findById(request.instructorId())
                .orElseThrow(() -> new NotFoundException("Instructor not found with id: " + request.instructorId()));
        return Course.builder()
                .title(request.title())
                .description(request.description())
                .category(category)
                .instructor(instructor)
                .price(request.price())
                .discountedPrice(request.discountedPrice())
                .discountRate((long) ((request.price() - request.discountedPrice()) / (double) request.price() * 100))
                .enrollmentCount(0L)
                .isPublished(request.isPublished())
                .isInSubscription(request.isInSubscription())
                .duration(request.duration())
                .sections(request.sections().stream()
                        .map(sectionRequest ->
                                Section.builder()
                                        .title(sectionRequest.title())
                                        .description(sectionRequest.description())
                                        .orderIndex(sectionRequest.orderIndex())
                                        .duration(sectionRequest.duration())
                                        .lectures(sectionRequest.lectures().stream()
                                                .map(lectureRequest ->
                                                        Lecture.builder()
                                                                .title(lectureRequest.title())
                                                                .lectureType(lectureRequest.lectureType())
                                                                .duration(lectureRequest.duration())
                                                                .orderIndex(lectureRequest.orderIndex())
                                                                .build()
                                                )
                                                .toList()
                                        )
                                        .build()
                        ).toList()
                )
                .build();
    }

    @Override
    public Course updateCourse(String courseId, Course courseDetails) {
        return null;
    }

    @Override
    public void deleteCourse(String courseId) {

    }
}
