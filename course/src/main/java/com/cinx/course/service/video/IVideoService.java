package com.cinx.course.service.video;

import com.cinx.course.dto.request.CreateVideoLessonRequest;
import com.cinx.course.dto.response.VideoLessonResponse;

public interface IVideoService {
        VideoLessonResponse getVideoByLessonId(String lessonId);
        void createVideo(String lessonId, CreateVideoLessonRequest request);
        void updateVideo(String lessonId, CreateVideoLessonRequest request);
        void deleteVideo(String lessonId);
}
