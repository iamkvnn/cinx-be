package com.cinx.course.service.course;

import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.common.mapper.SortConverter;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.course.consts.CourseStatus;
import com.cinx.course.dto.request.CreateCourseRequest;
import com.cinx.course.dto.request.RejectCourseRequest;
import com.cinx.course.dto.request.UpdateCourseRequest;
import com.cinx.course.dto.response.*;
import com.cinx.course.mapper.CourseMapper;
import com.cinx.course.messaging.CourseEventProducer;
import com.cinx.course.messaging.event.CourseEvent;
import com.cinx.course.model.*;
import com.cinx.course.repository.CategoryRepository;
import com.cinx.course.repository.CourseRepository;
import com.cinx.course.repository.RejectCourseReasonRepository;
import com.cinx.course.service.section.ISectionService;
import com.cinx.course.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private final RejectCourseReasonRepository rejectCourseReasonRepository;
    private final ISectionService sectionService;
    private final CourseMapper courseMapper;
    private final CourseEventProducer courseEventProducer;
    private final UserService userService;

    @Override
    public CourseDetailResponse getCourseById(String courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        course.setSections(sectionService.getSectionsByCourseId(courseId));
        UserDto instructor = userService.getInstructorById(course.getInstructorId()).data();
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
    public Page<CourseResponse> getAllCourses(
            String query,
            String categoryId,
            String instructorId,
            Integer rating,
            Integer priceFrom,
            Integer priceTo,
            CourseStatus status,
            int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page - 1, size, SortConverter.toSort(sort));
        Page<Course> courses = courseRepository.findAll(query, categoryId, instructorId, rating, priceFrom, priceTo, status, pageable);
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
        CourseAggregate aggregate = new CourseAggregate(course, instructor);
        
        courseEventProducer.publishOrderCreatedEvent(new CourseEvent(
                courseMapper.toDetailDto(aggregate),
                LocalDateTime.now()
        ));
        return courseMapper.toDto(aggregate);
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
        course.setStatus(request.isPublished() ? CourseStatus.WAITING_APPROVAL : CourseStatus.DRAFT);
        course.setDiscountRate((long) ((course.getPrice() - course.getDiscountedPrice()) / (double) course.getPrice() * 100));
        return course;
    }

    @Transactional
    @Override
    public CourseResponse updateCourse(String courseId, UpdateCourseRequest request) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        courseMapper.partialUpdate(course, request);
        course.setStatus(request.isPublished() ? CourseStatus.WAITING_APPROVAL : CourseStatus.DRAFT);
        if (request.isPublished()) {
            rejectCourseReasonRepository.deleteByCourseId(courseId);
        }
        if (request.price() != null && request.discountedPrice() != null) {
            course.setDiscountRate((long) ((course.getPrice() - course.getDiscountedPrice()) / (double) course.getPrice() * 100));
        }
        course.setCategory(categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + request.categoryId())));
        courseRepository.save(course);
        course.setSections(sectionService.updateSections(course, request.sections()));
        UserDto instructor = userService.getInstructorById(course.getInstructorId()).data();
        CourseAggregate aggregate = new CourseAggregate(course, instructor);

        courseEventProducer.publishCourseUpdatedEvent(new CourseEvent(
                courseMapper.toDetailDto(aggregate),
                LocalDateTime.now()
        ));
        return courseMapper.toDto(aggregate);
    }

    @Override
    public CourseResponse approveCourse(String courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        if (course.getStatus() != CourseStatus.WAITING_APPROVAL) {
            throw new BadRequestException("Only courses waiting for approval can be approved");
        }
        course.setStatus(CourseStatus.PUBLISHED);
        return courseMapper.toDto(new CourseAggregate(
                courseRepository.save(course),
                userService.getInstructorById(course.getInstructorId()).data()
        ));
    }

    @Transactional
    @Override
    public CourseResponse rejectCourse(String courseId, RejectCourseRequest request) {
        if (rejectCourseReasonRepository.findByCourseId(courseId).isPresent()) {
            throw new BadRequestException("Course has already been rejected");
        }
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        course.setStatus(CourseStatus.REJECTED);
        rejectCourseReasonRepository.save(RejectCourseReason.builder()
                .courseId(courseId)
                .reason(request.reason())
                .build());
        return courseMapper.toDto(new CourseAggregate(
                courseRepository.save(course),
                userService.getInstructorById(course.getInstructorId()).data()
        ));
    }

    @Override
    public RejectCourseResponse getRejectReason(String courseId) {
        RejectCourseReason reason = rejectCourseReasonRepository.findByCourseId(courseId)
                .orElseThrow(() -> new NotFoundException("Reject reason not found for course id: " + courseId));
        return new RejectCourseResponse(
                reason.getCourseId(),
                reason.getReason()
        );
    }

    @Override
    @Transactional
    public void updateCourseRating(String courseId, Double rating) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new NotFoundException("Course not found"));
        course.setRating(rating);
        courseRepository.save(course);
    }

    @Override
    public void increaseEnrollmentCount(String courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new NotFoundException("Course not found"));
        course.setEnrollmentCount(course.getEnrollmentCount() + 1);
        courseRepository.save(course);
    }
}
