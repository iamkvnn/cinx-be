package com.cinx.course.service.section;

import com.cinx.common.exception.NotFoundException;
import com.cinx.course.dto.request.CreateSectionRequest;
import com.cinx.course.dto.request.MoveSectionRequest;
import com.cinx.course.dto.request.UpdateSectionRequest;
import com.cinx.course.dto.response.SectionPositionResponse;
import com.cinx.course.dto.response.SectionResponse;
import com.cinx.course.mapper.SectionMapper;
import com.cinx.course.model.Course;
import com.cinx.course.model.CourseDraft;
import com.cinx.course.model.Section;
import com.cinx.course.repository.CourseRepository;
import com.cinx.course.repository.SectionRepository;
import com.cinx.course.service.course.ICourseDraftService;
import com.cinx.course.utils.OrderIndexUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SectionService implements ISectionService {
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
    public SectionPositionResponse moveSection(String courseId, String sectionId, MoveSectionRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        CourseDraft draft = courseDraftService.getOrCreateDraft(course);
        List<Section> currentSections = sectionRepository.findDraftByDraftForUpdate(draft.getId());
        Map<String, Section> sectionsByStableId = currentSections.stream()
                .collect(Collectors.toMap(Section::getStableId, Function.identity()));
        Section movedSection = sectionsByStableId.get(sectionId);
        if (movedSection == null) {
            throw new NotFoundException("Section not found with id: " + sectionId);
        }

        List<Section> remainingSections = currentSections.stream()
                .filter(section -> !Objects.equals(section.getStableId(), sectionId))
                .collect(Collectors.toCollection(ArrayList::new));
        int targetIndex = OrderIndexUtils.insertionIndex(
                remainingSections,
                request.previousSectionId(),
                request.nextSectionId(),
                Section::getStableId,
                sectionId,
                "section"
        );
        Integer newOrderIndex = OrderIndexUtils.midpointOrderIndex(
                remainingSections,
                targetIndex,
                Section::getOrderIndex
        );
        if (newOrderIndex != null) {
            if (!Objects.equals(movedSection.getOrderIndex(), newOrderIndex)) {
                movedSection.setOrderIndex(newOrderIndex);
                sectionRepository.save(movedSection);
            }
            return new SectionPositionResponse(movedSection.getStableId(), movedSection.getOrderIndex());
        }

        List<Section> desiredSections = new ArrayList<>(remainingSections);
        desiredSections.add(targetIndex, movedSection);
        List<Section> changedSections = OrderIndexUtils.rebalance(
                desiredSections,
                Section::getOrderIndex,
                Section::setOrderIndex
        );
        if (!changedSections.isEmpty()) {
            sectionRepository.saveAll(changedSections);
        }
        return new SectionPositionResponse(movedSection.getStableId(), movedSection.getOrderIndex());
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
                .orElse(0) + OrderIndexUtils.ORDER_STEP;
    }
}
