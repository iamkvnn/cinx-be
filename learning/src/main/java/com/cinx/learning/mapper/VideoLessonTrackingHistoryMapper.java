package com.cinx.learning.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.common.mapper.CreateMapper;
import com.cinx.common.mapper.UpdateMapper;
import com.cinx.learning.dto.request.TrackingVideoLessonRequest;
import com.cinx.learning.dto.response.VideoLessonTrackingHistoryResponse;
import com.cinx.learning.model.VideoLessonTrackingHistory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VideoLessonTrackingHistoryMapper extends
        BaseMapper<VideoLessonTrackingHistory, VideoLessonTrackingHistoryResponse>,
        CreateMapper<VideoLessonTrackingHistory, TrackingVideoLessonRequest>,
        UpdateMapper<VideoLessonTrackingHistory, TrackingVideoLessonRequest> {
}
