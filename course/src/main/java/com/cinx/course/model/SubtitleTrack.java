package com.cinx.course.model;

import com.cinx.common.model.AuditableEntity;
import com.cinx.course.consts.SubtitleFormat;
import com.cinx.course.consts.SubtitleSource;
import com.cinx.course.consts.SubtitleStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_subtitle_track_video_language",
                        columnNames = {"video_lesson_id", "language_code"}
                )
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SubtitleTrack extends AuditableEntity {
    @Column(name = "language_code", nullable = false, length = 35)
    private String languageCode;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private String fileKey;

    private String originalFileKey;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = false)
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubtitleFormat format;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubtitleSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubtitleStatus status;

    @Column(nullable = false)
    private Boolean isDefault;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_lesson_id", nullable = false)
    private VideoLesson videoLesson;
}
