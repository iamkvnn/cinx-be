package com.cinx.course.service.video;

import com.cinx.course.dto.request.CreateVideoLessonRequest;
import com.cinx.course.dto.request.UpdateVideoLessonRequest;
import com.cinx.course.dto.response.VideoLessonResponse;

public interface IVideoService {
        VideoLessonResponse getVideoByLessonId(String currentUserId, String courseId, String lessonId);
        VideoLessonResponse getReadableVideoByLessonId(String currentUserId, String courseId, String lessonId);
        void createVideo(String currentUserId, String courseId, String lessonId, CreateVideoLessonRequest request);
        void updateVideo(String currentUserId, String courseId, String lessonId, UpdateVideoLessonRequest request);
}
