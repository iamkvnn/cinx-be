package com.cinx.course.service.video;

import com.cinx.common.exception.AlreadyExistException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.course.consts.LessonType;
import com.cinx.course.dto.request.CreateVideoLessonRequest;
import com.cinx.course.dto.request.UpdateVideoLessonRequest;
import com.cinx.course.dto.response.VideoLessonResponse;
import com.cinx.course.mapper.VideoLessonMapper;
import com.cinx.course.repository.VideoLessonRepository;
import com.cinx.course.service.lesson.ILessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoService implements IVideoService {
    private final VideoLessonRepository videoLessonRepository;
    private final VideoLessonMapper videoLessonMapper;
    private final ILessonService lessonService;

    @Value("${aws.s3.cdn-url}")
    private String cdnUrl;

    @Override
    public VideoLessonResponse getVideoByLessonId(String courseId, String lessonId) {
        lessonService.ensureLessonBelongsToCourse(courseId, lessonId, LessonType.VIDEO);
        return videoLessonRepository.findByLessonId(lessonId)
                .map(videoLessonMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Video not found for lessonId: " + lessonId));
    }

    @Override
    public void createVideo(String courseId, String lessonId, CreateVideoLessonRequest request) {
        lessonService.ensureLessonBelongsToCourse(courseId, lessonId, LessonType.VIDEO);
        videoLessonRepository.findByLessonId(lessonId).ifPresentOrElse(existing -> {
            throw new AlreadyExistException("Video already exists for lessonId: " + lessonId);
        },() -> {
            var videoLesson = videoLessonMapper.toModel(request);
            videoLesson.setLessonId(lessonId);
            videoLessonRepository.save(videoLesson);
        });
    }

    @Override
    public void updateVideo(String courseId, String lessonId, UpdateVideoLessonRequest request) {
        lessonService.ensureLessonBelongsToCourse(courseId, lessonId, LessonType.VIDEO);
        videoLessonRepository.findByLessonId(lessonId).ifPresentOrElse(existing -> {
            videoLessonMapper.partialUpdate(existing, request);
            if (request.getFileKey() != null) {
                existing.setVideoUrl(cdnUrl + "/" + request.getFileKey());
            }
            videoLessonRepository.save(existing);
        },() -> {
            throw new NotFoundException("Video not found for lessonId: " + lessonId);
        });
    }
}
