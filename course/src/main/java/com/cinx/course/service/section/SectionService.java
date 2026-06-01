package com.cinx.course.service.section;

import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.course.dto.request.CreateSectionRequest;
import com.cinx.course.dto.request.UpdateSectionRequest;
import com.cinx.course.dto.response.SectionResponse;
import com.cinx.course.mapper.SectionMapper;
import com.cinx.course.model.Course;
import com.cinx.course.model.CourseDraft;
import com.cinx.course.model.Section;
import com.cinx.course.repository.CourseRepository;
import com.cinx.course.repository.SectionRepository;
import com.cinx.course.service.course.ICourseDraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SectionService implements ISectionService {
    private static final int ORDER_STEP = 1024;

    private final CourseRepository courseRepository;
    private final ICourseDraftService courseDraftService;
    private final SectionRepository sectionRepository;
    private final SectionMapper sectionMapper;

    @Transactional
    @Override
    public SectionResponse createSection(String courseId, CreateSectionRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        CourseDraft draft = courseDraftService.getOrCreateDraft(course);
        Section section = sectionMapper.toModel(request);
        section.setStableId(UUID.randomUUID().toString());
        section.setDraft(draft);
        section.setOrderIndex(nextSectionOrderIndex(draft.getId()));
        return sectionMapper.toResponse(sectionRepository.save(section));
    }

    @Transactional
    @Override
    public SectionResponse updateSection(String courseId, String sectionId, UpdateSectionRequest request) {
        Section section = editableSection(courseId, sectionId);
        sectionMapper.partialUpdate(section, request);
        return sectionMapper.toResponse(sectionRepository.save(section));
    }

    @Transactional
    @Override
    public List<SectionResponse> reorderSections(String courseId, List<String> sectionIds) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        CourseDraft draft = courseDraftService.getOrCreateDraft(course);
        List<Section> currentSections = sectionRepository.findDraftByDraftForUpdate(draft.getId());
        List<Section> desiredSections = validateSectionOrder(currentSections, sectionIds);
        List<Section> changedSections = applySparseOrder(currentSections, desiredSections);
        if (!changedSections.isEmpty()) {
            sectionRepository.saveAll(changedSections);
        }
        return desiredSections.stream()
                .map(sectionMapper::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public void deleteSection(String courseId, String sectionId) {
        Section section = editableSection(courseId, sectionId);
        sectionRepository.delete(section);
    }

    @Override
    public Section editableSection(String courseId, String sectionId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        CourseDraft editableDraft = courseDraftService.getOrCreateDraft(course);
        return sectionRepository.findDraftSection(editableDraft.getId(), sectionId)
                .orElseThrow(() -> new NotFoundException("Section not found with id: " + sectionId));
    }

    private Integer nextSectionOrderIndex(String draftId) {
        return sectionRepository.findDraftByDraftForUpdate(draftId).stream()
                .map(Section::getOrderIndex)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + ORDER_STEP;
    }

    private List<Section> validateSectionOrder(List<Section> currentSections, List<String> sectionIds) {
        if (sectionIds == null || sectionIds.size() != currentSections.size()) {
            throw new BadRequestException("Section order must include all draft sections");
        }

        Map<String, Section> sectionsByStableId = currentSections.stream()
                .collect(Collectors.toMap(Section::getStableId, Function.identity()));
        Set<String> seen = new HashSet<>();
        List<Section> desiredSections = new ArrayList<>();
        for (String sectionId : sectionIds) {
            if (sectionId == null || !seen.add(sectionId)) {
                throw new BadRequestException("Section order contains duplicate or null section id");
            }
            Section section = sectionsByStableId.get(sectionId);
            if (section == null) {
                throw new BadRequestException("Section does not belong to this course draft: " + sectionId);
            }
            desiredSections.add(section);
        }
        return desiredSections;
    }

    private List<Section> applySparseOrder(List<Section> currentSections, List<Section> desiredSections) {
        if (sameSectionOrder(currentSections, desiredSections)) {
            return List.of();
        }

        Section movedSection = singleMovedSection(currentSections, desiredSections);
        if (movedSection != null) {
            Integer newOrderIndex = midpointOrderIndex(desiredSections, desiredSections.indexOf(movedSection));
            if (newOrderIndex != null) {
                movedSection.setOrderIndex(newOrderIndex);
                return List.of(movedSection);
            }
        }

        List<Section> changedSections = new ArrayList<>();
        for (int i = 0; i < desiredSections.size(); i++) {
            Section section = desiredSections.get(i);
            int orderIndex = (i + 1) * ORDER_STEP;
            if (!Objects.equals(section.getOrderIndex(), orderIndex)) {
                section.setOrderIndex(orderIndex);
                changedSections.add(section);
            }
        }
        return changedSections;
    }

    private boolean sameSectionOrder(List<Section> currentSections, List<Section> desiredSections) {
        if (currentSections.size() != desiredSections.size()) {
            return false;
        }
        for (int i = 0; i < currentSections.size(); i++) {
            if (!Objects.equals(currentSections.get(i).getStableId(), desiredSections.get(i).getStableId())) {
                return false;
            }
        }
        return true;
    }

    private Section singleMovedSection(List<Section> currentSections, List<Section> desiredSections) {
        List<String> currentIds = currentSections.stream().map(Section::getStableId).toList();
        List<String> desiredIds = desiredSections.stream().map(Section::getStableId).toList();
        Map<String, Section> currentByStableId = currentSections.stream()
                .collect(Collectors.toMap(Section::getStableId, Function.identity()));

        for (String candidateId : currentIds) {
            int desiredIndex = desiredIds.indexOf(candidateId);
            List<String> moved = new ArrayList<>(currentIds);
            moved.remove(candidateId);
            moved.add(desiredIndex, candidateId);
            if (moved.equals(desiredIds)) {
                return currentByStableId.get(candidateId);
            }
        }
        return null;
    }

    private Integer midpointOrderIndex(List<Section> desiredSections, int movedIndex) {
        Integer previous = movedIndex == 0 ? null : desiredSections.get(movedIndex - 1).getOrderIndex();
        Integer next = movedIndex == desiredSections.size() - 1 ? null : desiredSections.get(movedIndex + 1).getOrderIndex();
        return midpoint(previous, next);
    }

    private Integer midpoint(Integer previous, Integer next) {
        if (previous == null && next == null) {
            return ORDER_STEP;
        }
        if (previous == null) {
            return next > 1 ? next / 2 : null;
        }
        if (next == null) {
            return previous + ORDER_STEP;
        }
        return next - previous > 1 ? previous + (next - previous) / 2 : null;
    }
}
