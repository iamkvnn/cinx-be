package com.cinx.course.repository;

import com.cinx.course.model.AssignmentAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentAttachmentRepository extends JpaRepository<AssignmentAttachment, String> {
}
