package com.cinx.learning.service.video;

import com.cinx.common.exception.ForbiddenException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.learning.dto.request.CreateVideoNoteRequest;
import com.cinx.learning.dto.request.UpdateVideoNoteRequest;
import com.cinx.learning.dto.response.VideoNoteDto;
import com.cinx.learning.mapper.VideoNoteMapper;
import com.cinx.learning.model.VideoNote;
import com.cinx.learning.repository.VideoNoteRepository;
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

    @Override
    @Transactional
    public VideoNoteDto createNote(String userId, CreateVideoNoteRequest request) {
        VideoNote note = videoNoteMapper.toModel(request);
        note.setUserId(userId);
        note = videoNoteRepository.save(note);
        return videoNoteMapper.toDto(note);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoNoteDto> getNotesByLesson(String userId, String lessonId) {
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
            throw new ForbiddenException("You are not the owner of this note");
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
            throw new ForbiddenException("You are not the owner of this note");
        }
        
        videoNoteRepository.delete(note);
    }
}
