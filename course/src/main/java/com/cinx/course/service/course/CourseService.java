package com.cinx.course.service.course;

import com.cinx.common.exception.NotFoundException;
import com.cinx.course.dto.request.CreateCourseRequest;
import com.cinx.course.dto.response.CourseResponse;
import com.cinx.course.mapper.CourseMapper;
import com.cinx.course.model.Category;
import com.cinx.course.model.Course;
import com.cinx.course.repository.CategoryRepository;
import com.cinx.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService implements ICourseService {
    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final CourseMapper courseMapper;

    @Override
    public CourseResponse getCourseById(String courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        return courseMapper.toDto(course);
    }

    @Override
    public List<CourseResponse> getCourseByIds(List<String> courseIds) {
        List<Course> courses = courseRepository.findAllById(courseIds);
        return courses.stream().map(courseMapper::toDto).toList();
    }

    @Override
    public Page<CourseResponse> getAllCourses(String query, Pageable pageable) {
        Page<Course> courses = courseRepository.findAll(query, pageable);
        return courses.map(courseMapper::toDto);
    }

    @Override
    public CourseResponse createCourse(CreateCourseRequest request) {
        Category category = categoryRepository.findByName(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + request.categoryId()));
        Course course = courseRepository.save(Course.builder()
                .title(request.title())
                .description(request.description())
                .category(category)
                .price(request.price())
                .duration(request.duration())
                .build());
        return courseMapper.toDto(course);
    }

    @Override
    public Course updateCourse(String courseId, Course courseDetails) {
        return null;
    }

    @Override
    public void deleteCourse(String courseId) {

    }
}
