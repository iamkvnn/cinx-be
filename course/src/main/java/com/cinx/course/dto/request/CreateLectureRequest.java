package com.cinx.course.dto.request;

import com.cinx.course.consts.LectureType;

public record CreateLectureRequest (
        String title,
        Long duration,
        Integer orderIndex,
        LectureType lectureType
) {
}
