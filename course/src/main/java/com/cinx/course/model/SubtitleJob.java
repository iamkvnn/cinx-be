package com.cinx.course.model;

import com.cinx.common.model.AuditableEntity;
import com.cinx.course.consts.SubtitleJobStatus;
import com.cinx.course.consts.SubtitleJobType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(indexes = {
        @Index(name = "idx_subtitle_job_video_status", columnList = "video_lesson_id,status"),
        @Index(name = "idx_subtitle_job_target_language", columnList = "video_lesson_id,target_language_code")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SubtitleJob extends AuditableEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubtitleJobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubtitleJobStatus status;

    private String sourceSubtitleId;

    private String outputSubtitleId;

    @Column(length = 35)
    private String sourceLanguageCode;

    @Column(nullable = false, length = 35)
    private String targetLanguageCode;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private String expectedOutputFileKey;

    @Column(nullable = false)
    private Integer progressPercent;

    private String errorCode;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_lesson_id", nullable = false)
    private VideoLesson videoLesson;
}
