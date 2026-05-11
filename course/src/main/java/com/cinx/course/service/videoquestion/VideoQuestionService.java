package com.cinx.course.service.videoquestion;

import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.course.dto.request.CreateVideoQuestionRequest;
import com.cinx.course.dto.request.UpdateVideoQuestionRequest;
import com.cinx.course.dto.response.VideoOptionResponse;
import com.cinx.course.dto.response.VideoQuestionResponse;
import com.cinx.course.mapper.VideoOptionMapper;
import com.cinx.course.mapper.VideoQuestionMapper;
import com.cinx.course.model.VideoLesson;
import com.cinx.course.model.VideoOption;
import com.cinx.course.model.VideoQuestion;
import com.cinx.course.repository.VideoLessonRepository;
import com.cinx.course.repository.VideoOptionRepository;
import com.cinx.course.repository.VideoQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoQuestionService implements IVideoQuestionService {

    private final VideoQuestionRepository videoQuestionRepository;
    private final VideoOptionRepository videoOptionRepository;
    private final VideoLessonRepository videoLessonRepository;
    private final VideoQuestionMapper videoQuestionMapper;
    private final VideoOptionMapper videoOptionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<VideoQuestionResponse> getQuestionsByLessonId(String lessonId) {
        VideoLesson videoLesson = videoLessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Video lesson not found"));
        
        List<VideoQuestion> questions = videoQuestionRepository.findByVideoLessonLessonIdOrderByTimestampSecondsAsc(lessonId);
        
        String currentUserId = AuthenticationUtil.extractUserId();
        boolean isInstructor = false;
        
        if (currentUserId != null && videoLesson.getLesson() != null 
            && videoLesson.getLesson().getSection() != null 
            && videoLesson.getLesson().getSection().getCourse() != null) {
            isInstructor = currentUserId.equals(videoLesson.getLesson().getSection().getCourse().getInstructorId());
        }

        if (isInstructor) {
            return questions.stream().map(videoQuestionMapper::toDto).collect(Collectors.toList());
        } else {
            return questions.stream()
                    .map(videoQuestionMapper::toDto)
                    .map(q -> new VideoQuestionResponse(
                            q.id(),
                            q.questionText(),
                            q.questionType(),
                            q.timestampSeconds(),
                            q.options().stream()
                                    .map(o -> new VideoOptionResponse(o.id(), o.optionText(), null))
                                    .toList()
                    ))
                    .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public VideoQuestionResponse getQuestionById(String id) {
        VideoQuestion question = videoQuestionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Video question not found"));
                
        VideoLesson videoLesson = question.getVideoLesson();
        
        String currentUserId = AuthenticationUtil.extractUserId();
        boolean isInstructor = false;
        
        if (currentUserId != null && videoLesson != null && videoLesson.getLesson() != null 
            && videoLesson.getLesson().getSection() != null 
            && videoLesson.getLesson().getSection().getCourse() != null) {
            isInstructor = currentUserId.equals(videoLesson.getLesson().getSection().getCourse().getInstructorId());
        }

        VideoQuestionResponse q = videoQuestionMapper.toDto(question);
        if (isInstructor) {
            return q;
        } else {
            return new VideoQuestionResponse(
                    q.id(),
                    q.questionText(),
                    q.questionType(),
                    q.timestampSeconds(),
                    q.options().stream()
                            .map(o -> new VideoOptionResponse(o.id(), o.optionText(), null))
                            .toList()
            );
        }
    }

    @Override
    @Transactional
    public VideoQuestionResponse createQuestion(String lessonId, CreateVideoQuestionRequest request) {
        VideoLesson videoLesson = videoLessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Video lesson not found"));

        if (request.timestampSeconds() < 0 || request.timestampSeconds() > videoLesson.getDuration()) {
            throw new BadRequestException("Timestamp must be between 0 and video duration (" + videoLesson.getDuration() + "s)");
        }

        VideoQuestion question = videoQuestionMapper.toModel(request);
        question.setVideoLesson(videoLesson);
        
        List<VideoOption> options = request.options().stream().map(optionRequest -> {
            VideoOption option = videoOptionMapper.toModel(optionRequest);
            option.setVideoQuestion(question);
            return option;
        }).toList();
        
        question.setOptions(options);

        VideoQuestion savedQuestion = videoQuestionRepository.save(question);
        return videoQuestionMapper.toDto(savedQuestion);
    }

    @Override
    @Transactional
    public VideoQuestionResponse updateQuestion(String id, UpdateVideoQuestionRequest request) {
        VideoQuestion question = videoQuestionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Video question not found"));

        VideoLesson videoLesson = question.getVideoLesson();
        if (request.timestampSeconds() < 0 || request.timestampSeconds() > videoLesson.getDuration()) {
            throw new BadRequestException("Timestamp must be between 0 and video duration (" + videoLesson.getDuration() + "s)");
        }

        videoQuestionMapper.partialUpdate(question, request);

        // Remove old options and create new ones based on the request
        videoOptionRepository.deleteAll(question.getOptions());
        question.getOptions().clear();

        List<VideoOption> newOptions = request.options().stream().map(optionRequest -> {
            VideoOption option = videoOptionMapper.toModel(optionRequest);
            option.setVideoQuestion(question);
            return option;
        }).toList();
        
        question.getOptions().addAll(newOptions);

        VideoQuestion updatedQuestion = videoQuestionRepository.save(question);
        return videoQuestionMapper.toDto(updatedQuestion);
    }

    @Override
    @Transactional
    public void deleteQuestion(String id) {
        VideoQuestion question = videoQuestionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Video question not found"));
        videoQuestionRepository.delete(question);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkAnswer(String questionId, String userAnswer) {
        VideoQuestion question = videoQuestionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Video question not found"));
                
        List<VideoOption> correctOptions = question.getOptions().stream()
                .filter(VideoOption::getIsCorrect)
                .toList();
                
        if (correctOptions.isEmpty()) {
            return false;
        }
        
        String correctStr = correctOptions.stream()
                .sorted()
                .toList()
                .toString();

        return userAnswer.equals(correctStr);
    }
}