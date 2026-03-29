package com.cinx.course.service.course;

import com.cinx.common.exception.NotFoundException;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.course.dto.request.CreateCourseRequest;
import com.cinx.course.dto.request.UpdateCourseRequest;
import com.cinx.course.dto.response.CourseAggregate;
import com.cinx.course.dto.response.CourseDetailResponse;
import com.cinx.course.dto.response.CourseResponse;
import com.cinx.course.dto.response.UserDto;
import com.cinx.course.mapper.CourseMapper;
import com.cinx.course.messaging.CourseEventProducer;
import com.cinx.course.messaging.event.CourseEvent;
import com.cinx.course.model.*;
import com.cinx.course.repository.CategoryRepository;
import com.cinx.course.repository.CourseRepository;
import com.cinx.course.service.section.ISectionService;
import com.cinx.course.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CourseService implements ICourseService {
    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final ISectionService sectionService;
    private final CourseMapper courseMapper;
    private final CourseEventProducer courseEventProducer;
    private final UserService userService;

    @Override
    public CourseDetailResponse getCourseById(String courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        course.setSections(sectionService.getSectionsByCourseId(courseId));
        UserDto instructor = userService.getInstructorById(course.getInstructorId()).data();
        System.out.println("Instructor: " + instructor);
        return courseMapper.toDetailDto(new CourseAggregate(
                course,
                instructor
        ));
    }

    @Override
    public List<CourseResponse> getCourseByIds(List<String> courseIds) {
        List<Course> courses = courseRepository.findAllById(courseIds);
        Map<String, UserDto> instructorMap = userService.getInstructorsByIds(courses.stream().map(Course::getInstructorId).toList()).data()
                .stream().collect(java.util.stream.Collectors.toMap(UserDto::userId, instructor -> instructor));
        return courses.stream().map(course -> courseMapper.toDto(new CourseAggregate(
                course,
                instructorMap.get(course.getInstructorId())
        ))).toList();
    }

    @Override
    public Page<CourseResponse> getAllCourses(String query, String categoryId, Pageable pageable) {
        Page<Course> courses = courseRepository.findAll(query, categoryId, pageable);
        Map<String, UserDto> instructorMap = userService.getInstructorsByIds(courses.stream().map(Course::getInstructorId).toList()).data()
                .stream().collect(java.util.stream.Collectors.toMap(UserDto::userId, instructor -> instructor));
        return courses.map(course -> courseMapper.toDto(new CourseAggregate(
                course,
                instructorMap.get(course.getInstructorId())
        )));
    }

    @Transactional
    @Override
    public CourseResponse createCourse(CreateCourseRequest request) {
        Course course = courseRepository.save(buildCourseFromRequest(request));
        UserDto instructor = userService.getInstructorById(course.getInstructorId()).data();
        CourseResponse courseResponse = courseMapper.toDto(new CourseAggregate(
                course,
                instructor
        ));
        courseEventProducer.publishOrderCreatedEvent(new CourseEvent(
                courseResponse,
                LocalDateTime.now()
        ));
        return courseResponse;
    }

    private Course buildCourseFromRequest(CreateCourseRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + request.categoryId()));
        Course course = courseMapper.toModel(request);
        course.getSections().forEach(section -> section.setCourse(course));
        course.getSections().forEach(section -> section.getLessons().forEach(lesson -> lesson.setSection(section)));
        course.setCategory(category);
        course.setInstructorId(userId);
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
        courseRepository.save(course);
        course.setSections(sectionService.updateSections(course, request.sections()));
        courseEventProducer.publishCourseUpdatedEvent(new CourseEvent(
                courseMapper.toDto(course),
                LocalDateTime.now()
        ));
        UserDto instructor = userService.getInstructorById(course.getInstructorId()).data();
        return courseMapper.toDto(new CourseAggregate(
                course,
                instructor
        ));
    }
}
