package com.cinx.course.service.subtitle;

import com.cinx.common.dto.PresignedUrlResponse;
import com.cinx.common.exception.AlreadyExistException;
import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.ErrorCode;
import com.cinx.common.exception.ForbiddenException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.course.consts.SubtitleFormat;
import com.cinx.course.consts.SubtitleSource;
import com.cinx.course.consts.SubtitleStatus;
import com.cinx.course.dto.request.CreateSubtitleTrackRequest;
import com.cinx.course.dto.request.UpdateSubtitleContentRequest;
import com.cinx.course.dto.request.UpdateSubtitleTrackRequest;
import com.cinx.course.dto.response.SubtitleContentResponse;
import com.cinx.course.dto.response.SubtitleTrackResponse;
import com.cinx.course.dto.response.SubtitleWordConfidenceResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cinx.course.mapper.SubtitleTrackMapper;
import com.cinx.course.model.SubtitleTrack;
import com.cinx.course.model.VideoLesson;
import com.cinx.course.repository.SubtitleTrackRepository;
import com.cinx.course.repository.VideoLessonRepository;
import com.cinx.course.service.lesson.ILessonService;
import com.cinx.course.service.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubtitleTrackService implements ISubtitleTrackService {
    private static final String SUBTITLE_UPLOAD_PREFIX = "courses/subtitles/uploads/";
    private static final String SUBTITLE_STORAGE_PREFIX = "courses/subtitles/";

    private final SubtitleTrackRepository subtitleTrackRepository;
    private final VideoLessonRepository videoLessonRepository;
    private final SubtitleTrackMapper subtitleTrackMapper;
    private final SubtitleFileProcessor subtitleFileProcessor;
    private final S3Service s3Service;
    private final ILessonService lessonService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SubtitleTrackResponse> getSubtitlesByLessonId(String lessonId) {
        ensureVideoLessonExists(lessonId);
        return subtitleTrackRepository.findByVideoLessonLessonIdOrderByIsDefaultDescLanguageCodeAsc(lessonId)
                .stream()
                .map(subtitleTrackMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PresignedUrlResponse getSubtitlePresignedUrl(
            String lessonId,
            String fileName,
            String contentType,
            String languageCode
    ) {
        ensureInstructor(lessonId);
        String normalizedLanguage = subtitleFileProcessor.normalizeLanguageCode(languageCode);
        subtitleFileProcessor.detectFormat(fileName, contentType);

        String fileKey = SUBTITLE_UPLOAD_PREFIX
                + lessonId + "/"
                + normalizedLanguage + "/"
                + UUID.randomUUID() + "-"
                + sanitizeFileName(fileName);
        return PresignedUrlResponse.builder()
                .fileKey(fileKey)
                .presignedUrl(s3Service.generatePresignedUrl(fileKey, contentType))
                .build();
    }

    @Override
    @Transactional
    public SubtitleTrackResponse createSubtitle(String lessonId, CreateSubtitleTrackRequest request) {
        VideoLesson videoLesson = ensureInstructor(lessonId);
        String languageCode = subtitleFileProcessor.normalizeLanguageCode(request.languageCode());
        subtitleTrackRepository.findByVideoLessonLessonIdAndLanguageCode(lessonId, languageCode)
                .ifPresent(existing -> {
                    throw new AlreadyExistException(ErrorCode.RESOURCE_ALREADY_EXISTS, "Subtitle already exists for language: " + languageCode);
                });

        SubtitleTrack subtitleTrack = new SubtitleTrack();
        applyUploadedFile(subtitleTrack, lessonId, languageCode, request.fileKey(), request.fileName(), request.fileType(), request.fileSize());
        subtitleTrack.setLanguageCode(languageCode);
        subtitleTrack.setDisplayName(request.displayName().trim());
        subtitleTrack.setSource(SubtitleSource.MANUAL);
        subtitleTrack.setStatus(SubtitleStatus.READY);
        subtitleTrack.setVideoLesson(videoLesson);
        subtitleTrack.setIsDefault(resolveDefaultFlag(lessonId, request.isDefault()));
        demoteOtherDefaultsIfNeeded(lessonId, subtitleTrack);

        return subtitleTrackMapper.toDto(subtitleTrackRepository.save(subtitleTrack));
    }

    @Override
    @Transactional
    public SubtitleTrackResponse updateSubtitle(String lessonId, String subtitleId, UpdateSubtitleTrackRequest request) {
        ensureInstructor(lessonId);
        SubtitleTrack subtitleTrack = subtitleTrackRepository.findByIdAndVideoLessonLessonId(subtitleId, lessonId)
                .orElseThrow(() -> new NotFoundException("Subtitle not found with id: " + subtitleId));

        subtitleTrackMapper.partialUpdate(subtitleTrack, request);
        if (request.displayName() != null && request.displayName().isBlank()) {
            throw new BadRequestException(ErrorCode.SUBTITLE_INVALID, "Subtitle display name must not be blank");
        }
        if (hasNewFile(request)) {
            if (request.fileKey() == null || request.fileName() == null || request.fileType() == null || request.fileSize() == null) {
                throw new BadRequestException(ErrorCode.SUBTITLE_FILE_INVALID, "fileKey, fileName, fileType and fileSize are required when replacing subtitle file");
            }
            applyUploadedFile(
                    subtitleTrack,
                    lessonId,
                    subtitleTrack.getLanguageCode(),
                    request.fileKey(),
                    request.fileName(),
                    request.fileType(),
                    request.fileSize()
            );
            subtitleTrack.setStatus(SubtitleStatus.READY);
            subtitleTrack.setSource(SubtitleSource.MANUAL);
            subtitleTrack.setWordConfidenceFileKey(null);
            subtitleTrack.setWordConfidenceFileUrl(null);
        }
        if (request.isDefault() != null) {
            subtitleTrack.setIsDefault(request.isDefault());
        }
        if (Boolean.TRUE.equals(subtitleTrack.getIsDefault())) {
            demoteOtherDefaultsIfNeeded(lessonId, subtitleTrack);
        } else {
            promoteAnotherDefaultIfNeeded(lessonId, subtitleTrack);
        }
        if (subtitleTrackRepository.countByVideoLessonLessonId(lessonId) == 1) {
            subtitleTrack.setIsDefault(true);
        }

        return subtitleTrackMapper.toDto(subtitleTrackRepository.save(subtitleTrack));
    }

    @Override
    @Transactional(readOnly = true)
    public SubtitleContentResponse getSubtitleContent(String lessonId, String subtitleId) {
        ensureInstructor(lessonId);
        SubtitleTrack subtitleTrack = findSubtitle(lessonId, subtitleId);
        return new SubtitleContentResponse(subtitleTrack.getId(), s3Service.readTextObject(subtitleTrack.getFileKey()));
    }

    @Override
    @Transactional
    public SubtitleTrackResponse updateSubtitleContent(String lessonId, String subtitleId, UpdateSubtitleContentRequest request) {
        ensureInstructor(lessonId);
        SubtitleTrack subtitleTrack = findSubtitle(lessonId, subtitleId);
        String normalized = subtitleFileProcessor.normalizeToWebVtt(SubtitleFormat.VTT, request.content());
        String editedFileKey = SUBTITLE_STORAGE_PREFIX
                + lessonId + "/"
                + subtitleTrack.getLanguageCode() + "/"
                + UUID.randomUUID() + "-edited.vtt";

        s3Service.uploadTextFile(editedFileKey, normalized, SubtitleFileProcessor.NORMALIZED_CONTENT_TYPE);
        subtitleTrack.setOriginalFileKey(subtitleTrack.getFileKey());
        subtitleTrack.setFileKey(editedFileKey);
        subtitleTrack.setFileUrl(s3Service.publicUrl(editedFileKey));
        subtitleTrack.setFileName(subtitleTrack.getId() + "-edited.vtt");
        subtitleTrack.setFileType(SubtitleFileProcessor.NORMALIZED_CONTENT_TYPE);
        subtitleTrack.setFileSize((long) normalized.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
        subtitleTrack.setFormat(SubtitleFormat.VTT);
        subtitleTrack.setSource(SubtitleSource.MANUAL);
        subtitleTrack.setWordConfidenceFileKey(null);
        subtitleTrack.setWordConfidenceFileUrl(null);
        subtitleTrack.setStatus(SubtitleStatus.READY);
        if (request.displayName() != null) {
            if (request.displayName().isBlank()) {
                throw new BadRequestException(ErrorCode.SUBTITLE_INVALID, "Subtitle display name must not be blank");
            }
            subtitleTrack.setDisplayName(request.displayName().trim());
        }
        return subtitleTrackMapper.toDto(subtitleTrackRepository.save(subtitleTrack));
    }

    @Override
    @Transactional(readOnly = true)
    public SubtitleWordConfidenceResponse getSubtitleWordConfidence(String lessonId, String subtitleId) {
        ensureInstructor(lessonId);
        SubtitleTrack subtitleTrack = findSubtitle(lessonId, subtitleId);
        String confidenceFileKey = subtitleTrack.getWordConfidenceFileKey();
        if (confidenceFileKey == null || confidenceFileKey.isBlank()) {
            throw new NotFoundException("AI word confidence file not found for subtitle: " + subtitleId);
        }
        try {
            String content = s3Service.readTextObject(confidenceFileKey);
            List<SubtitleWordConfidenceResponse.SubtitleWordConfidenceItem> words = objectMapper.readValue(
                    content,
                    new TypeReference<>() {}
            );
            return new SubtitleWordConfidenceResponse(subtitleTrack.getId(), words);
        } catch (JsonProcessingException ex) {
            throw new BadRequestException(ErrorCode.SUBTITLE_FILE_INVALID, "AI word confidence file is invalid");
        } catch (RuntimeException ex) {
            throw new NotFoundException("AI word confidence file not found for subtitle: " + subtitleId);
        }
    }

    private void promoteAnotherDefaultIfNeeded(String lessonId, SubtitleTrack selectedSubtitle) {
        boolean hasAnotherDefault = subtitleTrackRepository.findByVideoLessonLessonIdAndIsDefaultTrue(lessonId)
                .stream()
                .anyMatch(track -> !Objects.equals(track.getId(), selectedSubtitle.getId()));
        if (hasAnotherDefault) {
            return;
        }
        subtitleTrackRepository.findByVideoLessonLessonIdOrderByIsDefaultDescLanguageCodeAsc(lessonId)
                .stream()
                .filter(track -> !Objects.equals(track.getId(), selectedSubtitle.getId()))
                .findFirst()
                .ifPresent(nextDefault -> {
                    nextDefault.setIsDefault(true);
                    subtitleTrackRepository.save(nextDefault);
                });
    }

    @Override
    @Transactional
    public void deleteSubtitle(String lessonId, String subtitleId) {
        ensureInstructor(lessonId);
        SubtitleTrack subtitleTrack = subtitleTrackRepository.findByIdAndVideoLessonLessonId(subtitleId, lessonId)
                .orElseThrow(() -> new NotFoundException("Subtitle not found with id: " + subtitleId));
        boolean wasDefault = Boolean.TRUE.equals(subtitleTrack.getIsDefault());
        subtitleTrackRepository.delete(subtitleTrack);
        subtitleTrackRepository.flush();

        if (wasDefault) {
            subtitleTrackRepository.findByVideoLessonLessonIdOrderByIsDefaultDescLanguageCodeAsc(lessonId)
                    .stream()
                    .findFirst()
                    .ifPresent(nextDefault -> {
                        nextDefault.setIsDefault(true);
                        subtitleTrackRepository.save(nextDefault);
                    });
        }
    }

    private VideoLesson ensureVideoLessonExists(String lessonId) {
        return videoLessonRepository.findByLessonId(lessonId)
                .orElseThrow(() -> new NotFoundException("Video lesson not found for lessonId: " + lessonId));
    }

    private SubtitleTrack findSubtitle(String lessonId, String subtitleId) {
        return subtitleTrackRepository.findByIdAndVideoLessonLessonId(subtitleId, lessonId)
                .orElseThrow(() -> new NotFoundException("Subtitle not found with id: " + subtitleId));
    }

    private VideoLesson ensureInstructor(String lessonId) {
        VideoLesson videoLesson = ensureVideoLessonExists(lessonId);
        String currentUserId = AuthenticationUtil.extractUserId();
        if (!lessonService.isLessonInstructor(lessonId, currentUserId)) {
            throw new ForbiddenException(ErrorCode.INSTRUCTOR_ACCESS_REQUIRED, "You do not have permission to manage subtitles for this lesson");
        }
        return videoLesson;
    }

    private void applyUploadedFile(
            SubtitleTrack subtitleTrack,
            String lessonId,
            String languageCode,
            String sourceFileKey,
            String fileName,
            String fileType,
            Long fileSize
    ) {
        subtitleFileProcessor.validateFileSize(fileSize);
        SubtitleFormat sourceFormat = subtitleFileProcessor.detectFormat(fileName, fileType);
        String uploadedContent = s3Service.readTextObject(sourceFileKey);
        String webVttContent = subtitleFileProcessor.normalizeToWebVtt(sourceFormat, uploadedContent);
        String normalizedFileKey = SUBTITLE_STORAGE_PREFIX
                + lessonId + "/"
                + languageCode + "/"
                + UUID.randomUUID() + ".vtt";

        s3Service.uploadTextFile(normalizedFileKey, webVttContent, SubtitleFileProcessor.NORMALIZED_CONTENT_TYPE);

        subtitleTrack.setOriginalFileKey(sourceFileKey);
        subtitleTrack.setFileKey(normalizedFileKey);
        subtitleTrack.setFileUrl(s3Service.publicUrl(normalizedFileKey));
        subtitleTrack.setFileName(fileName.trim());
        subtitleTrack.setFileType(SubtitleFileProcessor.NORMALIZED_CONTENT_TYPE);
        subtitleTrack.setFileSize((long) webVttContent.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
        subtitleTrack.setFormat(SubtitleFormat.VTT);
    }

    private boolean resolveDefaultFlag(String lessonId, Boolean requestedDefault) {
        return Boolean.TRUE.equals(requestedDefault) || subtitleTrackRepository.countByVideoLessonLessonId(lessonId) == 0;
    }

    private void demoteOtherDefaultsIfNeeded(String lessonId, SubtitleTrack selectedDefault) {
        if (!Boolean.TRUE.equals(selectedDefault.getIsDefault())) {
            return;
        }
        List<SubtitleTrack> defaults = subtitleTrackRepository.findByVideoLessonLessonIdAndIsDefaultTrue(lessonId)
                .stream()
                .filter(track -> !Objects.equals(track.getId(), selectedDefault.getId()))
                .toList();
        defaults.forEach(track -> track.setIsDefault(false));
        subtitleTrackRepository.saveAll(defaults);
    }

    private boolean hasNewFile(UpdateSubtitleTrackRequest request) {
        return request.fileKey() != null || request.fileName() != null || request.fileType() != null || request.fileSize() != null;
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new BadRequestException(ErrorCode.SUBTITLE_FILE_INVALID, "Subtitle file name is required");
        }
        return fileName.trim().replaceAll("[\\\\/]+", "-");
    }
}
