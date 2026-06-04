package com.cinx.course.service.course;

import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.ForbiddenException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.common.mapper.SortConverter;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.course.consts.CourseStatus;
import com.cinx.course.dto.request.CreateCourseRequest;
import com.cinx.course.dto.request.RejectCourseRequest;
import com.cinx.course.dto.request.UpdateCourseRequest;
import com.cinx.course.dto.response.CourseResponse;
import com.cinx.course.dto.response.InstructorCourseSummaryResponse;
import com.cinx.course.dto.response.RejectCourseResponse;
import com.cinx.course.dto.response.UserDto;
import com.cinx.course.mapper.CourseMapper;
import com.cinx.course.messaging.CourseEventProducer;
import com.cinx.course.messaging.event.CourseRecommendationEvent;
import com.cinx.course.messaging.event.CourseRecommendationPayload;
import com.cinx.course.model.Category;
import com.cinx.course.model.Course;
import com.cinx.course.model.CourseDraft;
import com.cinx.course.model.RejectCourseReason;
import com.cinx.course.repository.CategoryRepository;
import com.cinx.course.repository.CourseRepository;
import com.cinx.course.repository.RejectCourseReasonRepository;
import com.cinx.course.service.curriculum.ICurriculumService;
import com.cinx.course.service.enrollment.EnrollmentClient;
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
import java.util.Objects;
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
    private final EnrollmentClient enrollmentClient;
    private final ICurriculumService curriculumService;
    private final CourseMapper courseMapper;
    private final CourseEventProducer courseEventProducer;

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getPublishedCourseById(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        if (!isBuyable(course)) {
            throw new NotFoundException("Course not found with id: " + courseId);
        }
        return toResponse(course);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getEnrolledCourseById(String courseId) {
        Course course = findEnrolledReadableCourse(courseId);
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
        Map<String, Course> courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));
        return toResponse(distinctCourseIds.stream()
                .map(courseMap::get)
                .filter(Objects::nonNull)
                .toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getEnrolledCourseByIds(List<String> courseIds) {
        List<String> distinctCourseIds = courseIds.stream()
                .distinct()
                .toList();
        List<Course> courses = courseRepository.findEnrolledReadableByIds(distinctCourseIds);
        Map<String, Course> courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));
        return toResponse(distinctCourseIds.stream()
                .map(courseMap::get)
                .filter(Objects::nonNull)
                .toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InstructorCourseSummaryResponse getInstructorCourseSummary(String instructorId) {
        long courseCount = courseRepository.countByInstructorId(instructorId);
        long publishedCourseCount = courseRepository.countByInstructorIdAndIsPublishedTrue(instructorId);
        Double averageRating = courseRepository.averageRatingByInstructorId(instructorId);
        return new InstructorCourseSummaryResponse(
                courseCount,
                publishedCourseCount,
                averageRating != null ? averageRating : 0.0
        );
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

    @Override
    @Transactional
    public CourseResponse archiveCourse(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        String userId = AuthenticationUtil.extractUserId();
        if (!Objects.equals(course.getInstructorId(), userId)) {
            throw new ForbiddenException("You are not allowed to archive this course");
        }
        if (!Boolean.TRUE.equals(course.getIsPublished()) || course.getStatus() == CourseStatus.ARCHIVED) {
            throw new BadRequestException("Only published, non-archived courses can be archived");
        }
        course.setStatus(CourseStatus.ARCHIVED);
        Course savedCourse = courseRepository.save(course);
        publishRecommendationEvent(savedCourse, "course.course.archived", "CourseArchived", false);
        return toResponse(savedCourse);
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
        publishRecommendationEvent(savedCourse, "course.course.published", "CoursePublished", true);
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
        Course savedCourse = courseRepository.save(course);
        publishRecommendationEvent(savedCourse, "course.course.updated", "CourseUpdated", false);
    }

    @Override
    @Transactional
    public void increaseEnrollmentCount(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        course.setEnrollmentCount(course.getEnrollmentCount() == null ? 1L : course.getEnrollmentCount() + 1);
        Course savedCourse = courseRepository.save(course);
        publishRecommendationEvent(savedCourse, "course.course.updated", "CourseUpdated", false);
    }

    @Override
    @Transactional
    public void replayRecommendationEvents() {
        courseRepository.findAll().stream()
                .filter(course -> Boolean.TRUE.equals(course.getIsPublished()))
                .forEach(course -> publishRecommendationEvent(course, "course.course.updated", "CourseUpdated", true));
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
        if (courses.isEmpty()) {
            return List.of();
        }
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

    private Course findEnrolledReadableCourse(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        if (!Boolean.TRUE.equals(course.getIsPublished())) {
            throw new NotFoundException("Course not found with id: " + courseId);
        }
        return course;
    }

    private boolean isBuyable(Course course) {
        return Boolean.TRUE.equals(course.getIsPublished()) && course.getStatus() != CourseStatus.ARCHIVED;
    }

    private void ensureCurrentUserEnrolled(String courseId) {
        boolean enrolled = enrollmentClient.checkEnrollmentStatus(List.of(courseId)).data().stream()
                .anyMatch(status -> courseId.equals(status.courseId()) && Boolean.TRUE.equals(status.enrolled()));
        if (!enrolled) {
            throw new ForbiddenException("You are not enrolled in this course");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getEnrolledCourseByIdForCurrentUser(String courseId) {
        ensureCurrentUserEnrolled(courseId);
        return getEnrolledCourseById(courseId);
    }

    private void publishRecommendationEvent(Course course, String routingKey, String eventType, boolean includeCurriculum) {
        CourseResponse response = toResponse(course);
        CourseRecommendationPayload payload = new CourseRecommendationPayload(
                response.id(),
                response.title(),
                response.description(),
                response.category(),
                response.instructor(),
                response.images(),
                response.price(),
                response.discountedPrice(),
                response.discountRate(),
                response.rating(),
                response.enrollmentCount(),
                course.getIsPublished(),
                response.isInSubscription(),
                response.duration(),
                response.hasCertificate(),
                response.certificateTitle(),
                response.status(),
                includeCurriculum ? curriculumService.getEnrolledCurriculum(course.getId()).sections() : null,
                response.createdAt(),
                response.updatedAt()
        );
        courseEventProducer.publishCourseRecommendationEvent(
                routingKey,
                eventType,
                new CourseRecommendationEvent(payload, LocalDateTime.now())
        );
    }
}
