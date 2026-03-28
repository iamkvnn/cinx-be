package com.cinx.course.service.course;

import com.cinx.common.exception.NotFoundException;
import com.cinx.course.dto.request.CreateCourseRequest;
import com.cinx.course.dto.request.UpdateCourseRequest;
import com.cinx.course.dto.response.CourseDetailResponse;
import com.cinx.course.dto.response.CourseResponse;
import com.cinx.course.mapper.CourseMapper;
import com.cinx.course.messaging.CourseEventProducer;
import com.cinx.course.messaging.event.CourseEvent;
import com.cinx.course.model.*;
import com.cinx.course.repository.CategoryRepository;
import com.cinx.course.repository.CourseRepository;
import com.cinx.course.repository.InstructorRepository;
import com.cinx.course.service.section.ISectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService implements ICourseService {
    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final ISectionService sectionService;
    private final InstructorRepository instructorRepository;
    private final CourseMapper courseMapper;
    private final CourseEventProducer courseEventProducer;

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
        courseEventProducer.publishOrderCreatedEvent(new CourseEvent(
                courseMapper.toDto(course),
                LocalDateTime.now()
        ));
        return courseMapper.toDto(course);
    }

    private Course buildCourseFromRequest(CreateCourseRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + request.categoryId()));
        Instructor instructor = instructorRepository.findById(request.instructorId())
                .orElseThrow(() -> new NotFoundException("Instructor not found with id: " + request.instructorId()));
        Course course = courseMapper.toModel(request);
        course.getSections().forEach(section -> section.setCourse(course));
        course.getSections().forEach(section -> section.getLessons().forEach(lesson -> lesson.setSection(section)));
        course.setCategory(category);
        course.setInstructor(instructor);
        course.setEnrollmentCount(0L);
        course.setDiscountRate((long) ((course.getPrice() - course.getDiscountedPrice()) / (double) course.getPrice() * 100));
        return course;
    }

    @Transactional
    @Override
    public CourseResponse updateCourse(String courseId, UpdateCourseRequest request) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        courseMapper.partialUpdate(course, request);
        course.setCategory(categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + request.categoryId())));
        course.setInstructor(instructorRepository.findById(request.instructorId())
                .orElseThrow(() -> new NotFoundException("Instructor not found with id: " + request.instructorId())));
        courseRepository.save(course);
        course.setSections(sectionService.updateSections(course, request.sections()));
        courseEventProducer.publishCourseUpdatedEvent(new CourseEvent(
                courseMapper.toDto(course),
                LocalDateTime.now()
        ));
        return courseMapper.toDto(course);
    }
}
