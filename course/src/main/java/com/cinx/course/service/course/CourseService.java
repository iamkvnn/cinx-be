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
import com.cinx.course.service.change.ICourseChangeAuditService;
import com.cinx.course.service.section.ISectionService;
import com.cinx.course.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ICourseChangeAuditService courseChangeAuditService;

    @Override
    public CourseResponse getCourseById(String courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        UserDto instructor = userService.getInstructorById(course.getInstructorId()).data();
        return courseMapper.toDto(new CourseAggregate(
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
        return courseMapper.toDto(aggregate);
    }

    private Course buildCourseFromRequest(CreateCourseRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + request.categoryId()));
        Course course = courseMapper.toModel(request);
        course.setCategory(category);
        course.setInstructorId(userId);
        course.setEnrollmentCount(0L);
        course.setStatus(CourseStatus.DRAFT);
        course.setDiscountRate((long) ((course.getPrice() - course.getDiscountedPrice()) / (double) course.getPrice() * 100));
        return course;
    }

    @Transactional
    @Override
    public CourseResponse updateCourse(String courseId, UpdateCourseRequest request) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        UserDto instructor = userService.getInstructorById(course.getInstructorId()).data();
        CourseResponse oldValue = courseMapper.toDto(new CourseAggregate(course, instructor));
        courseMapper.partialUpdate(course, request);
        course.setStatus(CourseStatus.DRAFT);
        if (request.price() != null && request.discountedPrice() != null) {
            course.setDiscountRate((long) ((course.getPrice() - course.getDiscountedPrice()) / (double) course.getPrice() * 100));
        }
        course.setCategory(categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + request.categoryId())));
        courseRepository.save(course);
        CourseAggregate aggregate = new CourseAggregate(course, instructor);
        CourseResponse newValue = courseMapper.toDto(aggregate);
        courseChangeAuditService.auditCourseChange(courseId, oldValue, newValue);
        return newValue;
    }

    @Override
    public CourseResponse publishCourse(String courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        if (course.getStatus() != CourseStatus.DRAFT) {
            throw new BadRequestException("Only courses in draft status can be published");
        }
        course.setStatus(CourseStatus.WAITING_APPROVAL);
        return courseMapper.toDto(new CourseAggregate(
                courseRepository.save(course),
                userService.getInstructorById(course.getInstructorId()).data()
        ));
    }

    @Transactional
    @Override
    public CourseResponse approveCourse(String courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        if (course.getStatus() != CourseStatus.WAITING_APPROVAL) {
            throw new BadRequestException("Only courses waiting for approval can be approved");
        }
        course.setStatus(CourseStatus.PUBLISHED);
        courseChangeAuditService.deleteCourseChangeHistory(courseId);
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
    public void draftCourse(String courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        course.setStatus(CourseStatus.DRAFT);
        courseRepository.save(course);
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
