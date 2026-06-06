package com.cinx.learning.service.video;

import com.cinx.common.exception.ErrorCode;
import com.cinx.common.exception.ForbiddenException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.learning.dto.request.CreateVideoNoteRequest;
import com.cinx.learning.dto.request.UpdateVideoNoteRequest;
import com.cinx.learning.dto.response.VideoNoteDto;
import com.cinx.learning.mapper.VideoNoteMapper;
import com.cinx.learning.model.VideoNote;
import com.cinx.learning.repository.VideoNoteRepository;
import com.cinx.learning.service.course.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoNoteService implements IVideoNoteService {
    private final VideoNoteRepository videoNoteRepository;
    private final VideoNoteMapper videoNoteMapper;
    private final CourseService courseService;

    @Override
    @Transactional
    public VideoNoteDto createNote(String courseId, String lessonId, String userId, CreateVideoNoteRequest request) {
        courseService.getVideoLessonById(courseId, lessonId);
        VideoNote note = videoNoteMapper.toModel(request);
        note.setCourseId(courseId);
        note.setLessonId(lessonId);
        note.setUserId(userId);
        note = videoNoteRepository.save(note);
        return videoNoteMapper.toDto(note);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoNoteDto> getNotesByLesson(String courseId, String userId, String lessonId) {
        courseService.getVideoLessonById(courseId, lessonId);
        return videoNoteRepository.findByUserIdAndLessonId(userId, lessonId)
                .stream().map(videoNoteMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoNoteDto> getNotesByCourse(String userId, String courseId) {
        return videoNoteRepository.findByUserIdAndCourseId(userId, courseId)
                .stream().map(videoNoteMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VideoNoteDto updateNote(String userId, String noteId, UpdateVideoNoteRequest request) {
        VideoNote note = videoNoteRepository.findById(noteId)
                .orElseThrow(() -> new NotFoundException("Video note not found"));
        
        if (!note.getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.NOT_RESOURCE_OWNER, "You are not the owner of this note");
        }
        
        videoNoteMapper.partialUpdate(note, request);
        note = videoNoteRepository.save(note);
        return videoNoteMapper.toDto(note);
    }

    @Override
    @Transactional
    public void deleteNote(String userId, String noteId) {
        VideoNote note = videoNoteRepository.findById(noteId)
                .orElseThrow(() -> new NotFoundException("Video note not found"));
                
        if (!note.getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.NOT_RESOURCE_OWNER, "You are not the owner of this note");
        }
        
        videoNoteRepository.delete(note);
    }

}
