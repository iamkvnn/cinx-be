package com.cinx.course.service.course;

import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.common.mapper.SortConverter;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.course.consts.CourseStatus;
import com.cinx.course.dto.request.CreateCourseRequest;
import com.cinx.course.dto.request.RejectCourseRequest;
import com.cinx.course.dto.request.UpdateCourseRequest;
import com.cinx.course.dto.response.CourseResponse;
import com.cinx.course.dto.response.RejectCourseResponse;
import com.cinx.course.dto.response.UserDto;
import com.cinx.course.mapper.CourseMapper;
import com.cinx.course.messaging.CourseEventProducer;
import com.cinx.course.model.Category;
import com.cinx.course.model.Course;
import com.cinx.course.model.CourseDraft;
import com.cinx.course.model.RejectCourseReason;
import com.cinx.course.repository.CategoryRepository;
import com.cinx.course.repository.CourseRepository;
import com.cinx.course.repository.RejectCourseReasonRepository;
import com.cinx.course.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService implements ICourseService {
    private final CourseRepository courseRepository;
    private final RejectCourseReasonRepository rejectCourseReasonRepository;
    private final CategoryRepository categoryRepository;
    private final ICourseDraftService courseDraftService;
    private final UserService userService;
    private final CourseMapper courseMapper;
    private final CourseEventProducer courseEventProducer;

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getPublishedCourseById(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        if (!Boolean.TRUE.equals(course.getIsPublished())) {
            throw new NotFoundException("Course not found with id: " + courseId);
        }
        return toResponse(course);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseById(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        CourseDraft draft = courseDraftService.findDraft(course).orElse(null);
        return toResponse(course, draft);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getPublishedCourseByIds(List<String> courseIds) {
        List<String> distinctCourseIds = courseIds.stream()
                .distinct()
                .toList();
        List<Course> courses = courseRepository.findPublishedByIds(distinctCourseIds);
        if (courses.size() != distinctCourseIds.size()) {
            throw new NotFoundException("Some courses not found with ids: " + distinctCourseIds);
        }
        Map<String, Course> courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));
        return toResponse(distinctCourseIds.stream()
                .map(courseMap::get)
                .toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponse> getAllPublishedCourses(
            String query,
            String categoryId,
            String instructorId,
            Integer rating,
            Integer priceFrom,
            Integer priceTo,
            int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page - 1, size, SortConverter.toSort(sort));
        Page<Course> courses = courseRepository.searchPublished(query, categoryId, instructorId, rating, priceFrom, priceTo, pageable);
        return toResponse(courses);
    }

    @Override
    @Transactional(readOnly = true)
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
        Page<Course> courses = courseRepository.searchAll(query, categoryId, instructorId, rating, priceFrom, priceTo, status, pageable);
        return toResponse(courses);
    }

    @Transactional
    @Override
    public CourseResponse createCourse(CreateCourseRequest request) {
        Course course = buildCourseFromRequest(request);
        return toResponse(courseRepository.save(course));
    }

    private Course buildCourseFromRequest(CreateCourseRequest request) {
        Course course = courseMapper.toModel(request);
        course.setInstructorId(AuthenticationUtil.extractUserId());
        course.setEnrollmentCount(0L);
        course.setRating(0.0);
        course.setStatus(CourseStatus.DRAFT);
        course.setDiscountRate(calculateDiscountRate(request.price(), request.discountedPrice()));
        course.setCategory(category(request.categoryId()));
        return course;
    }

    @Transactional
    @Override
    public CourseResponse updateCourse(String courseId, UpdateCourseRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        Category category = category(request.categoryId());
        Long discountRate = calculateDiscountRate(request.price(), request.discountedPrice());
        if (Boolean.TRUE.equals(course.getIsPublished()) || courseDraftService.findDraft(course).isPresent()) {
            CourseDraft draft = courseDraftService.updateDraft(course, request, category, discountRate);
            return toResponse(course, draft);
        }
        courseMapper.partialUpdate(course, request);
        course.setCategory(category);
        if (request.price() != null || request.discountedPrice() != null) {
            course.setDiscountRate(discountRate);
        }
        course.setStatus(CourseStatus.DRAFT);
        return toResponse(courseRepository.save(course));
    }

    @Override
    @Transactional
    public CourseResponse submitCourse(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        if (course.getStatus() != CourseStatus.DRAFT && course.getStatus() != CourseStatus.REJECTED) {
            throw new BadRequestException("Only draft or rejected courses can be submitted");
        }
        Optional<CourseDraft> draft = courseDraftService.findDraft(course);
        if (draft.isEmpty() && course.getIsPublished()) {
            throw new BadRequestException("Course draft not found for course id: " + courseId);
        }
        course.setStatus(CourseStatus.WAITING_APPROVAL);
        return toResponse(courseRepository.save(course), draft.orElse(null));
    }

    @Transactional
    @Override
    public CourseResponse approveCourse(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        if (course.getStatus() != CourseStatus.WAITING_APPROVAL) {
            throw new BadRequestException("Only courses waiting for approval can be approved");
        }
        var lessonChangedEvents = courseDraftService.approveDraft(course);
        course.setStatus(CourseStatus.PUBLISHED);
        course.setIsPublished(true);
        Course savedCourse = courseRepository.save(course);
        lessonChangedEvents.forEach(courseEventProducer::publishLessonChangedEvent);
        return toResponse(savedCourse);
    }

    @Transactional
    @Override
    public CourseResponse rejectCourse(String courseId, RejectCourseRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        CourseDraft draft = courseDraftService.findDraft(course).orElse(null);
        course.setStatus(CourseStatus.REJECTED);
        RejectCourseReason rejectReason = rejectCourseReasonRepository.findByCourse(courseId)
                .orElseGet(() -> RejectCourseReason.builder()
                        .courseId(courseId)
                        .build());
        rejectReason.setReason(request.reason());
        rejectCourseReasonRepository.save(rejectReason);
        return toResponse(courseRepository.save(course), draft);
    }

    @Override
    public RejectCourseResponse getRejectReason(String courseId) {
        RejectCourseReason reason = rejectCourseReasonRepository.findByCourse(courseId)
                .orElseThrow(() -> new NotFoundException("Reject reason not found for course id: " + courseId));
        return new RejectCourseResponse(reason.getCourseId(), reason.getReason());
    }

    @Override
    @Transactional
    public void updateCourseRating(String courseId, Double rating) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        course.setRating(rating);
        courseRepository.save(course);
    }

    @Override
    @Transactional
    public void increaseEnrollmentCount(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        course.setEnrollmentCount(course.getEnrollmentCount() == null ? 1L : course.getEnrollmentCount() + 1);
        courseRepository.save(course);
    }

    private CourseResponse toResponse(Course course) {
        UserDto instructor = userService.getInstructorById(course.getInstructorId()).data();
        return courseMapper.toResponse(course, instructor);
    }

    private CourseResponse toResponse(Course course, CourseDraft draft) {
        if (draft != null) {
            UserDto instructor = userService.getInstructorById(course.getInstructorId()).data();
            return courseMapper.toResponse(course, draft, instructor);
        }
        return toResponse(course);
    }

    private Page<CourseResponse> toResponse(Page<Course> courses) {
        Map<String, UserDto> instructorMap = userService.getInstructorsByIds(courses.stream()
                .map(Course::getInstructorId)
                .toList()).data()
                .stream().collect(Collectors.toMap(UserDto::userId, Function.identity()));
        return courses.map(course -> courseMapper.toResponse(course, instructorMap.get(course.getInstructorId())));
    }

    private List<CourseResponse> toResponse(List<Course> courses) {
        Map<String, UserDto> instructorMap = userService.getInstructorsByIds(courses.stream()
                .map(Course::getInstructorId)
                .toList()).data()
                .stream().collect(Collectors.toMap(UserDto::userId, Function.identity()));
        return courses.stream()
                .map(course -> courseMapper.toResponse(course, instructorMap.get(course.getInstructorId())))
                .toList();
    }

    private Category category(String categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + categoryId));
    }

    private Long calculateDiscountRate(Long price, Long discountedPrice) {
        if (price == null || price == 0 || discountedPrice == null) {
            return 0L;
        }
        return (long) ((price - discountedPrice) / (double) price * 100);
    }
}
