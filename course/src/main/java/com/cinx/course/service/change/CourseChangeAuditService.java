package com.cinx.course.service.change;

import com.cinx.course.dto.response.CourseChangeResponse;
import com.cinx.course.dto.response.CourseResponse;
import com.cinx.course.mapper.CourseChangeMapper;
import com.cinx.course.model.CourseChange;
import com.cinx.course.repository.CourseChangeRepository;
import com.cinx.course.utils.JsonConverter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for auditing course update.
 * Only keeps track of the latest change for each course and item from the last publish.
 * This means that if a course is updated multiple times before being published, only the most recent change will be stored in the audit log.
 * After the course is published, the change history will be cleared as it is no longer needed and to save storage space.
 */
@Service
@RequiredArgsConstructor
public class CourseChangeAuditService implements ICourseChangeAuditService {
    private final CourseChangeRepository courseChangeRepository;
    private final CourseChangeMapper courseChangeMapper;
    private final JsonConverter jsonConverter;

    @Transactional
    @Override
    public void auditCourseChange(@NonNull String courseId, @NonNull CourseResponse oldValue, @NonNull CourseResponse newValue) {
        Optional<CourseChange> opt = courseChangeRepository.findByCourseIdAndItemIdIsNull(courseId);
        if (opt.isEmpty()) {
            courseChangeRepository.save(CourseChange.builder()
                    .courseId(courseId)
                    .oldValue(jsonConverter.toJson(oldValue))
                    .newValue(jsonConverter.toJson(newValue))
                    .build());
            return;
        }
        CourseChange change = opt.get();
        if (change.getOldValue().equals(jsonConverter.toJson(newValue))) {
            courseChangeRepository.delete(change);
        }
        else {
            change.setNewValue(jsonConverter.toJson(newValue));
            courseChangeRepository.save(change);
        }
    }

    @Transactional
    @Override
    public void auditCourseItemChange(@NonNull String courseId, @NonNull String itemId, Object oldValue, Object newValue) {
        Optional<CourseChange> opt = courseChangeRepository.findByCourseIdAndItemId(courseId, itemId);
        if (opt.isEmpty()) {
            courseChangeRepository.save(CourseChange.builder()
                    .courseId(courseId)
                    .itemId(itemId)
                    .oldValue(oldValue != null ? jsonConverter.toJson(oldValue) : null)
                    .newValue(newValue != null ? jsonConverter.toJson(newValue) : null)
                    .build());
            return;
        }
        CourseChange change = opt.get();

        if (newValue == null || (change.getOldValue() != null && change.getOldValue().equals(jsonConverter.toJson(newValue)))) {
            courseChangeRepository.delete(change);
        }
        else {
            change.setNewValue(jsonConverter.toJson(newValue));
            courseChangeRepository.save(change);
        }
    }

    @Override
    public List<CourseChangeResponse> getCourseChangeHistory(String courseId) {
        return courseChangeRepository.findAllByCourseId(courseId).stream().map(
                courseChangeMapper::toDto
        ).toList();
    }

    // Will be called when course is published or deleted, as the change history is no longer needed and should be cleared to save storage space
    @Transactional
    @Override
    public void deleteCourseChangeHistory(String courseId) {
        courseChangeRepository.deleteAllByCourseId(courseId);
    }
}
