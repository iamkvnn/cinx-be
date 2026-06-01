package com.cinx.learning.service.video;

import com.cinx.learning.dto.request.CreateVideoNoteRequest;
import com.cinx.learning.dto.request.UpdateVideoNoteRequest;
import com.cinx.learning.dto.response.VideoNoteDto;

import java.util.List;

public interface IVideoNoteService {
    VideoNoteDto createNote(String courseId, String lessonId, String userId, CreateVideoNoteRequest request);
    List<VideoNoteDto> getNotesByLesson(String courseId, String userId, String lessonId);
    List<VideoNoteDto> getNotesByCourse(String userId, String courseId);
    VideoNoteDto updateNote(String userId, String noteId, UpdateVideoNoteRequest request);
    void deleteNote(String userId, String noteId);
}
