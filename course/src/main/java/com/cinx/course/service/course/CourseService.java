package com.cinx.course.service.course;

import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.ErrorCode;
import com.cinx.common.exception.NotFoundException;
import com.cinx.common.mapper.SortConverter;
import com.cinx.course.consts.CoursePublishStatus;
import com.cinx.course.consts.CourseStatus;
import com.cinx.course.dto.request.CreateCourseRequest;
import com.cinx.course.dto.request.RejectCourseRequest;
import com.cinx.course.dto.request.UpdateCourseRequest;
import com.cinx.course.dto.response.*;
import com.cinx.course.mapper.CourseMapper;
import com.cinx.course.messaging.CourseEventProducer;
import com.cinx.course.messaging.event.CourseApprovalRequestedEvent;
import com.cinx.course.messaging.event.CourseContentPublishedEvent;
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
import com.cinx.course.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final ICourseAccessService courseAccessService;
    private final ICurriculumService curriculumService;
    private final CourseMapper courseMapper;
    private final CourseEventProducer courseEventProducer;

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getReadableCourseById(String currentUserId, String courseId) {
        return toResponse(courseAccessService.ensureReadableCourse(currentUserId, courseId));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getEditableDraftCourseById(String currentUserId, String courseId) {
        Course course = courseAccessService.ensureManageableCourse(currentUserId, courseId);
        return draftOnlyResponse(course);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getReadableCourseByIds(String currentUserId, List<String> courseIds) {
        List<String> distinctCourseIds = courseIds.stream()
                .distinct()
                .toList();
        List<Course> courses = courseRepository.findEnrolledReadableByIds(distinctCourseIds);
        Map<String, Course> courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));
        Map<String, Boolean> enrollmentByCourseId = courseAccessService.enrollmentByCourseId(currentUserId, courses);
        return toResponse(distinctCourseIds.stream()
                .map(courseMap::get)
                .filter(Objects::nonNull)
                .filter(course -> courseAccessService.canReadCourse(currentUserId, course, enrollmentByCourseId))
                .toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InstructorCourseSummaryResponse getInstructorCourseSummary(String instructorId) {
        long courseCount = courseRepository.countByInstructorId(instructorId);
        long publishedCourseCount = courseRepository.countByInstructorIdAndStatus(instructorId, CourseStatus.PUBLISHED);
        Double averageRating = courseRepository.averageRatingByInstructorId(instructorId);
        return new InstructorCourseSummaryResponse(
                courseCount,
                publishedCourseCount,
                averageRating
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
        Pageable pageable = PageRequest.of(page - 1, size, courseSort(sort));
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
            CoursePublishStatus publishStatus,
            int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page - 1, size, courseSort(sort));
        Page<Course> courses = courseRepository.searchAll(query, categoryId, instructorId, rating, priceFrom, priceTo, status, publishStatus, pageable);
        return toResponse(courses);
    }

    @Transactional
    @Override
    public CourseResponse createCourse(String currentUserId, CreateCourseRequest request) {
        Course course = courseRepository.save(buildCourseFromRequest(currentUserId, request));
        CourseDraft draft = courseDraftService.createDraftFromCourse(course);
        return toResponse(course, draft);
    }

    private Course buildCourseFromRequest(String currentUserId, CreateCourseRequest request) {
        Course course = courseMapper.toModel(request);
        course.setInstructorId(currentUserId);
        course.setEnrollmentCount(0L);
        course.setRating(null);
        course.setStatus(CourseStatus.DRAFT);
        course.setPublishStatus(null);
        course.setDiscountRate(calculateDiscountRate(request.price(), request.discountedPrice()));
        course.setCategory(category(request.categoryId()));
        return course;
    }

    @Transactional
    @Override
    public CourseResponse updateCourse(String currentUserId, String courseId, UpdateCourseRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        courseAccessService.ensureCurrentUserOwns(currentUserId, course);
        if (course.getStatus() == CourseStatus.ARCHIVED) {
            throw new BadRequestException(ErrorCode.COURSE_ARCHIVED, "Archived courses cannot be updated");
        }
        Category category = category(request.categoryId());
        Long discountRate = calculateDiscountRate(request.price(), request.discountedPrice());
        CourseDraft draft = courseDraftService.updateDraft(course, request, category, discountRate);
        courseMapper.partialUpdate(course, request);
        course.setCategory(category);
        course.setDiscountRate(discountRate);
        course.setPublishStatus(null);
        return toResponse(courseRepository.save(course), draft);
    }

    @Override
    @Transactional
    public CourseResponse submitCourse(String currentUserId, String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        courseAccessService.ensureCurrentUserOwns(currentUserId, course);
        if (course.getStatus() == CourseStatus.ARCHIVED) {
            throw new BadRequestException(ErrorCode.COURSE_ARCHIVED, "Archived courses cannot be submitted");
        }
        if (course.getPublishStatus() == CoursePublishStatus.WAITING_APPROVAL) {
            throw new BadRequestException(ErrorCode.COURSE_WAITING_APPROVAL, "Course is already waiting for approval");
        }
        Optional<CourseDraft> draft = courseDraftService.findDraft(course);
        if (draft.isEmpty()) {
            throw new BadRequestException(ErrorCode.COURSE_DRAFT_MISSING, "Course draft not found for course id: " + courseId);
        }
        courseDraftService.ensureDraftReadyForSubmission(draft.get());
        course.setPublishStatus(CoursePublishStatus.WAITING_APPROVAL);
        Course savedCourse = courseRepository.save(course);
        courseEventProducer.publishCourseApprovalRequestedEvent(new CourseApprovalRequestedEvent(
                savedCourse.getId(),
                savedCourse.getTitle(),
                savedCourse.getInstructorId()
        ));
        return toResponse(savedCourse, draft.get());
    }

    @Override
    @Transactional
    public CourseResponse archiveCourse(String currentUserId, String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        courseAccessService.ensureCurrentUserOwns(currentUserId, course);
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new BadRequestException(ErrorCode.COURSE_STATUS_INVALID, "Only published, non-archived courses can be archived");
        }
        course.setStatus(CourseStatus.ARCHIVED);
        course.setPublishStatus(null);
        Course savedCourse = courseRepository.save(course);
        publishRecommendationEvent(savedCourse, "course.course.archived", "CourseArchived", false);
        return toResponse(savedCourse);
    }

    @Override
    @Transactional
    public CourseResponse unarchiveCourse(String currentUserId, String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        courseAccessService.ensureCurrentUserOwns(currentUserId, course);
        if (course.getStatus() != CourseStatus.ARCHIVED) {
            throw new BadRequestException(ErrorCode.COURSE_STATUS_INVALID, "Only archived courses can be unarchived");
        }
        course.setStatus(CourseStatus.PUBLISHED);
        course.setPublishStatus(null);
        Course savedCourse = courseRepository.save(course);
        publishRecommendationEvent(savedCourse, "course.course.published", "CoursePublished", true);
        return toResponse(savedCourse);
    }

    @Transactional
    @Override
    public CourseResponse approveCourse(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        if (course.getStatus() == CourseStatus.ARCHIVED) {
            throw new BadRequestException(ErrorCode.COURSE_ARCHIVED, "Archived courses cannot be approved");
        }
        if (course.getPublishStatus() != CoursePublishStatus.WAITING_APPROVAL) {
            throw new BadRequestException(ErrorCode.COURSE_STATUS_INVALID, "Only courses waiting for approval can be approved");
        }
        courseDraftService.approveDraft(course);
        course.setStatus(CourseStatus.PUBLISHED);
        course.setPublishStatus(CoursePublishStatus.PUBLISHED);
        Course savedCourse = courseRepository.save(course);
        courseEventProducer.publishCourseContentPublishedEvent(new CourseContentPublishedEvent(
                savedCourse.getId(),
                savedCourse.getTitle(),
                savedCourse.getInstructorId()
        ));
        publishRecommendationEvent(savedCourse, "course.course.published", "CoursePublished", true);
        return toResponse(savedCourse);
    }

    @Transactional
    @Override
    public CourseResponse rejectCourse(String courseId, RejectCourseRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        if (course.getStatus() == CourseStatus.ARCHIVED) {
            throw new BadRequestException(ErrorCode.COURSE_ARCHIVED, "Archived courses cannot be rejected");
        }
        if (course.getPublishStatus() != CoursePublishStatus.WAITING_APPROVAL) {
            throw new BadRequestException(ErrorCode.COURSE_STATUS_INVALID, "Only courses waiting for approval can be rejected");
        }
        CourseDraft draft = courseDraftService.findDraft(course).orElse(null);
        course.setPublishStatus(CoursePublishStatus.REJECTED);
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
                .filter(course -> course.getStatus() == CourseStatus.PUBLISHED)
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

    private CourseResponse draftOnlyResponse(Course course) {
        CourseDraft draft = courseDraftService.findDraft(course).orElse(null);
        if (draft != null) {
            return toResponse(course, draft);
        }
        if (course.getStatus() == CourseStatus.DRAFT) {
            return toResponse(course);
        }
        throw new NotFoundException("Course draft not found for course id: " + course.getId());
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

    private Sort courseSort(String sort) {
        return SortConverter.toSort(sort).stream()
                .map(order -> "rating".equals(order.getProperty())
                        ? order.with(Sort.NullHandling.NULLS_LAST)
                        : order)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Sort::by));
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
                response.isInSubscription(),
                response.duration(),
                response.hasCertificate(),
                response.certificateTitle(),
                response.status(),
                response.publishStatus(),
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
