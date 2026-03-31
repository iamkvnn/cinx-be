package com.cinx.learning.service.learningPath;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.learning.consts.LearningPathStatus;
import com.cinx.learning.dto.request.AddToCartRequest;
import com.cinx.learning.dto.request.LearningPathItemRequest;
import com.cinx.learning.dto.request.LearningPathRequest;
import com.cinx.learning.dto.response.CheckEnrollmentStatus;
import com.cinx.learning.dto.response.LearningPathResponse;
import com.cinx.learning.mapper.LearningPathMapper;
import com.cinx.learning.model.LearningPathItem;
import com.cinx.learning.model.UserLearningPath;
import com.cinx.learning.repository.CourseProgressRepository;
import com.cinx.learning.repository.LearningPathItemRepository;
import com.cinx.learning.repository.UserLearningPathRepository;
import com.cinx.learning.service.cart.CartClient;
import com.cinx.learning.service.enrollment.EnrollmentClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LearningPathService implements ILearningPathService {

    private final UserLearningPathRepository pathRepository;
    private final LearningPathItemRepository itemRepository;
    private final LearningPathMapper pathMapper;
    private final EnrollmentClient enrollmentClient;
    private final CartClient cartClient;
    private final CourseProgressRepository courseProgressRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LearningPathResponse> getLearningPaths(String userId) {
        return pathRepository.findByUserId(userId).stream()
                .map(pathMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LearningPathResponse getLearningPath(String userId, String id) {
        UserLearningPath path = pathRepository.findById(id)
                .filter(p -> p.getUserId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Learning path not found"));
        return pathMapper.toDto(path);
    }

    @Transactional
    @Override
    public LearningPathResponse createLearningPath(String userId, LearningPathRequest request) {
        pathRepository.findByUserIdAndStatusIn(
                userId, List.of(LearningPathStatus.ACTIVE, LearningPathStatus.PENDING_PAYMENT))
                .ifPresent(p -> {
                    throw new BadRequestException("You already have an active or pending learning path. Drop or complete it to create a new one.");
                });

        List<String> distinctCourseIds = request.getItems().stream()
                .map(LearningPathItemRequest::getCourseId)
                .distinct()
                .collect(Collectors.toList());

        ApiResponse<List<CheckEnrollmentStatus>> checkRes = enrollmentClient.checkEnrollmentStatus(distinctCourseIds);
        
        List<String> unenrolledCourseIds = checkRes.data().stream()
                .filter(en -> !en.isEnrolled())
                .map(CheckEnrollmentStatus::courseId)
                .toList();

        LearningPathStatus initialStatus = !unenrolledCourseIds.isEmpty() ? LearningPathStatus.PENDING_PAYMENT : LearningPathStatus.ACTIVE;

        UserLearningPath savedPath = pathRepository.save(
                UserLearningPath.builder()
                    .userId(userId)
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .status(initialStatus)
                    .currentProgress(0.0)
                    .totalItems(request.getItems().size())
                    .completedItems(0)
                    .build());

        savedPath.setItems(itemRepository.saveAll(request.getItems().stream()
                .<LearningPathItem>map(req ->
                    LearningPathItem.builder()
                        .learningPath(savedPath)
                        .courseId(req.getCourseId())
                        .lessonId(req.getLessonId())
                        .orderIndex(req.getOrderIndex())
                        .isSuggested(req.getIsSuggested())
                        .isCompleted(false)
                        .build()
        ).toList()));

        if (initialStatus == LearningPathStatus.PENDING_PAYMENT) {
            for (String cId : unenrolledCourseIds) {
                cartClient.addToCart(new AddToCartRequest(cId));
            }
        }

        return pathMapper.toDto(savedPath);
    }

    @Override
    @Transactional(readOnly = true)
    public LearningPathResponse getActiveLearningPath(String userId) {
        UserLearningPath path = pathRepository.findByUserIdAndStatusIn(
                userId, List.of(LearningPathStatus.ACTIVE, LearningPathStatus.PENDING_PAYMENT))
                .orElseThrow(() -> new NotFoundException("No active learning path found for user"));
        return pathMapper.toDto(path);
    }

    @Override
    public void dropActiveLearningPath(String userId) {
        UserLearningPath path = pathRepository.findByUserIdAndStatusIn(
                        userId, List.of(LearningPathStatus.ACTIVE, LearningPathStatus.PENDING_PAYMENT))
                .orElseThrow(() -> new NotFoundException("No active learning path found for user"));
        
        path.setStatus(LearningPathStatus.DROPPED);
        pathRepository.save(path);
    }

    @Override
    public void activatePendingPathForCourse(String userId, String courseId) {
        Optional<UserLearningPath> optPath = pathRepository.findByUserIdAndStatus(userId, LearningPathStatus.PENDING_PAYMENT);
        if (optPath.isEmpty()) return;

        UserLearningPath path = optPath.get();
        // Check if any other courses in this path are still unenrolled
        List<String> courseIdsInPath = path.getItems().stream()
                .map(LearningPathItem::getCourseId)
                .distinct()
                .toList();

        long enrolledCourseCount = courseProgressRepository.findAllByUserIdAndCourseIdIn(userId, courseIdsInPath).stream()
                .map(com.cinx.learning.model.CourseProgress::getCourseId)
                .distinct()
                .count();
        boolean allEnrolled = enrolledCourseCount == courseIdsInPath.size();

        if (allEnrolled) {
            path.setStatus(LearningPathStatus.ACTIVE);
            pathRepository.save(path);
        }
    }

    @Override
    public void updatePathProgress(String userId, String lessonId) {
        Optional<UserLearningPath> optPath = pathRepository.findByUserIdAndStatus(userId, LearningPathStatus.ACTIVE);
        if (optPath.isEmpty()) return;

        UserLearningPath path = optPath.get();
        Optional<LearningPathItem> optItem = itemRepository.findByLearningPathIdAndLessonId(path.getId(), lessonId);
        if (optItem.isPresent() && !optItem.get().getIsCompleted()) {
            LearningPathItem item = optItem.get();
            item.setIsCompleted(true);
            itemRepository.save(item);

            path.setCompletedItems(path.getCompletedItems() + 1);
            double progress = (double) path.getCompletedItems() / path.getTotalItems() * 100;
            path.setCurrentProgress(progress);

            if (path.getCompletedItems().equals(path.getTotalItems())) {
                path.setStatus(LearningPathStatus.COMPLETED);
            }
            pathRepository.save(path);
        }
    }
}
