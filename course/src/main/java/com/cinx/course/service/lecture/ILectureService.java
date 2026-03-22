package com.cinx.course.service.lecture;

import com.cinx.course.dto.request.CreateLectureRequest;
import com.cinx.course.model.Lecture;

import java.util.List;

public interface ILectureService {
    List<Lecture> getLecturesBySectionId(String sectionId);
    List<Lecture> getLecturesBySectionIds(List<String> lectureIds);
    List<Lecture> createLectures(List<Lecture> lectures);
    List<Lecture> updateLectures(List<Lecture> lectures);
}
