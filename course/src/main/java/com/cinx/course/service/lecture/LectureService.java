package com.cinx.course.service.lecture;

import com.cinx.course.model.Lecture;
import com.cinx.course.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LectureService implements ILectureService {
    private final LectureRepository lectureRepository;

    @Override
    public List<Lecture> getLecturesBySectionId(String sectionId) {
        return List.of();
    }

    @Override
    public List<Lecture> getLecturesBySectionIds(List<String> sectionIds) {
        return lectureRepository.findAllBySectionIdIn(sectionIds);
    }

    @Override
    public List<Lecture> createLectures(List<Lecture> lectures) {
        return lectureRepository.saveAll(lectures);
    }

    @Override
    public List<Lecture> updateLectures(List<Lecture> lectures) {
        return List.of();
    }
}
