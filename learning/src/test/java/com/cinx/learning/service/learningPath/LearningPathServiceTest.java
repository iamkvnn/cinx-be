package com.cinx.learning.service.learningPath;

import com.cinx.common.dto.ApiResponse;
import com.cinx.learning.consts.LearningPathStatus;
import com.cinx.learning.dto.request.LearningPathItemRequest;
import com.cinx.learning.dto.request.LearningPathRequest;
import com.cinx.learning.dto.response.CheckEnrollmentStatus;
import com.cinx.learning.dto.response.LearningPathResponse;
import com.cinx.learning.mapper.LearningPathMapper;
import com.cinx.learning.model.LearningItemProgress;
import com.cinx.learning.model.LearningPathItem;
import com.cinx.learning.model.UserLearningPath;
import com.cinx.learning.repository.CourseProgressRepository;
import com.cinx.learning.repository.LearningItemProgressRepository;
import com.cinx.learning.repository.LearningPathItemRepository;
import com.cinx.learning.repository.UserLearningPathRepository;
import com.cinx.learning.service.cart.CartClient;
import com.cinx.learning.service.enrollment.EnrollmentClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningPathServiceTest {
    @Mock
    private UserLearningPathRepository pathRepository;
    @Mock
    private LearningPathItemRepository itemRepository;
    @Mock
    private LearningPathMapper pathMapper;
    @Mock
    private EnrollmentClient enrollmentClient;
    @Mock
    private CartClient cartClient;
    @Mock
    private CourseProgressRepository courseProgressRepository;
    @Mock
    private LearningItemProgressRepository learningItemProgressRepository;

    @InjectMocks
    private LearningPathService learningPathService;

    @Test
    @SuppressWarnings("unchecked")
    void createLearningPathBackfillsCompletedLessons() {
        LearningPathRequest request = LearningPathRequest.builder()
                .title("Backend Path")
                .items(List.of(
                        itemRequest("course-1", "lesson-1", 0),
                        itemRequest("course-1", "lesson-2", 1)))
                .build();
        LearningItemProgress completedProgress = LearningItemProgress.builder()
                .itemId("lesson-1")
                .isCompleted(true)
                .build();
        ArgumentCaptor<UserLearningPath> pathCaptor = ArgumentCaptor.forClass(UserLearningPath.class);
        ArgumentCaptor<List<LearningPathItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);

        when(pathRepository.findByUserIdAndStatusIn(
                "user-1", List.of(LearningPathStatus.ACTIVE, LearningPathStatus.PENDING_PAYMENT)))
                .thenReturn(Optional.empty());
        when(enrollmentClient.checkEnrollmentStatus(List.of("course-1")))
                .thenReturn(new ApiResponse<>(true, "ok", List.of(new CheckEnrollmentStatus("course-1", true))));
        when(learningItemProgressRepository.findAllByUserIdAndItemIdIn(
                "user-1", List.of("lesson-1", "lesson-2")))
                .thenReturn(List.of(completedProgress));
        when(pathRepository.save(pathCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemRepository.saveAll(itemsCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(pathMapper.toDto(any(UserLearningPath.class))).thenReturn(new LearningPathResponse());

        learningPathService.createLearningPath("user-1", request);

        UserLearningPath savedPath = pathCaptor.getValue();
        assertThat(savedPath.getCompletedItems()).isEqualTo(1);
        assertThat(savedPath.getTotalItems()).isEqualTo(2);
        assertThat(savedPath.getCurrentProgress()).isEqualTo(50.0);
        assertThat(savedPath.getStatus()).isEqualTo(LearningPathStatus.ACTIVE);

        List<LearningPathItem> savedItems = itemsCaptor.getValue();
        assertThat(savedItems)
                .extracting(LearningPathItem::getLessonId, LearningPathItem::getIsCompleted)
                .containsExactly(
                        tuple("lesson-1", true),
                        tuple("lesson-2", false));
    }

    @Test
    void createLearningPathMarksCompletedWhenAllLessonsWereAlreadyCompleted() {
        LearningPathRequest request = LearningPathRequest.builder()
                .title("Finished Path")
                .items(List.of(
                        itemRequest("course-1", "lesson-1", 0),
                        itemRequest("course-1", "lesson-2", 1)))
                .build();

        when(pathRepository.findByUserIdAndStatusIn(
                "user-1", List.of(LearningPathStatus.ACTIVE, LearningPathStatus.PENDING_PAYMENT)))
                .thenReturn(Optional.empty());
        when(enrollmentClient.checkEnrollmentStatus(List.of("course-1")))
                .thenReturn(new ApiResponse<>(true, "ok", List.of(new CheckEnrollmentStatus("course-1", true))));
        when(learningItemProgressRepository.findAllByUserIdAndItemIdIn(
                "user-1", List.of("lesson-1", "lesson-2")))
                .thenReturn(List.of(completedItem("lesson-1"), completedItem("lesson-2")));
        when(pathRepository.save(any(UserLearningPath.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(pathMapper.toDto(any(UserLearningPath.class))).thenReturn(new LearningPathResponse());

        learningPathService.createLearningPath("user-1", request);

        ArgumentCaptor<UserLearningPath> pathCaptor = ArgumentCaptor.forClass(UserLearningPath.class);
        org.mockito.Mockito.verify(pathRepository).save(pathCaptor.capture());
        assertThat(pathCaptor.getValue().getCompletedItems()).isEqualTo(2);
        assertThat(pathCaptor.getValue().getCurrentProgress()).isEqualTo(100.0);
        assertThat(pathCaptor.getValue().getStatus()).isEqualTo(LearningPathStatus.COMPLETED);
    }

    private LearningPathItemRequest itemRequest(String courseId, String lessonId, int orderIndex) {
        return LearningPathItemRequest.builder()
                .courseId(courseId)
                .lessonId(lessonId)
                .orderIndex(orderIndex)
                .isSuggested(false)
                .build();
    }

    private LearningItemProgress completedItem(String itemId) {
        return LearningItemProgress.builder()
                .itemId(itemId)
                .isCompleted(true)
                .build();
    }
}
