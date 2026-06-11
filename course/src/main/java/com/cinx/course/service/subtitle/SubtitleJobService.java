package com.cinx.course.service.subtitle;

import com.cinx.common.exception.AlreadyExistException;
import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.ErrorCode;
import com.cinx.common.exception.ForbiddenException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.course.consts.LessonType;
import com.cinx.course.consts.SubtitleFormat;
import com.cinx.course.consts.SubtitleJobStatus;
import com.cinx.course.consts.SubtitleJobType;
import com.cinx.course.consts.SubtitleSource;
import com.cinx.course.consts.SubtitleStatus;
import com.cinx.course.dto.request.GenerateDefaultSubtitleJobRequest;
import com.cinx.course.dto.request.SubtitleJobCompletedRequest;
import com.cinx.course.dto.request.TranslateSubtitleJobRequest;
import com.cinx.course.dto.response.SubtitleJobResponse;
import com.cinx.course.mapper.SubtitleJobMapper;
import com.cinx.course.messaging.CourseEventProducer;
import com.cinx.course.messaging.event.SubtitleGenerateRequestedEvent;
import com.cinx.course.messaging.event.SubtitleTranslateRequestedEvent;
import com.cinx.course.model.SubtitleJob;
import com.cinx.course.model.SubtitleTrack;
import com.cinx.course.model.VideoLesson;
import com.cinx.course.repository.SubtitleJobRepository;
import com.cinx.course.repository.SubtitleTrackRepository;
import com.cinx.course.repository.VideoLessonRepository;
import com.cinx.course.service.lesson.ILessonService;
import com.cinx.course.service.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SubtitleJobService implements ISubtitleJobService {
    private static final String AI_SUBTITLE_STORAGE_PREFIX = "courses/subtitles/ai/";
    private static final String NORMALIZED_CONTENT_TYPE = "text/vtt";
    private static final String AUTO_LANGUAGE_CODE = "auto";

    private final SubtitleJobRepository subtitleJobRepository;
    private final SubtitleTrackRepository subtitleTrackRepository;
    private final VideoLessonRepository videoLessonRepository;
    private final SubtitleJobMapper subtitleJobMapper;
    private final WhisperLanguageRegistry whisperLanguageRegistry;
    private final CourseEventProducer courseEventProducer;
    private final ILessonService lessonService;
    private final S3Service s3Service;

    @Override
    @Transactional
    public SubtitleJobResponse createDefaultSubtitleJob(String courseId, String lessonId, GenerateDefaultSubtitleJobRequest request) {
        VideoLesson videoLesson = ensureInstructor(courseId, lessonId);
        if (subtitleTrackRepository.countByVideoLessonLessonId(lessonId) > 0) {
            throw new BadRequestException(ErrorCode.SUBTITLE_INVALID, "Default subtitle can only be generated when the video has no subtitles");
        }
        String languageCode = normalizeOptionalWhisperLanguage(request.languageCode());
        if (!AUTO_LANGUAGE_CODE.equals(languageCode)) {
            ensureNoExistingSubtitle(lessonId, languageCode);
        }
        ensureNoActiveJob(lessonId, languageCode);

        SubtitleJob job = newJob(videoLesson, SubtitleJobType.GENERATE_DEFAULT, null, null, languageCode, request.displayName());
        SubtitleJob saved = subtitleJobRepository.save(job);
        courseEventProducer.publishSubtitleGenerateRequestedEvent(new SubtitleGenerateRequestedEvent(
                saved.getId(),
                courseId,
                lessonId,
                videoLesson.getFileKey(),
                saved.getTargetLanguageCode(),
                saved.getDisplayName(),
                saved.getExpectedOutputFileKey()
        ));
        return subtitleJobMapper.toDto(saved);
    }

    @Override
    @Transactional
    public List<SubtitleJobResponse> createTranslationJobs(String courseId, String lessonId, TranslateSubtitleJobRequest request) {
        VideoLesson videoLesson = ensureInstructor(courseId, lessonId);
        SubtitleTrack source = resolveSourceSubtitle(lessonId, request.sourceSubtitleId());
        if (source.getStatus() != SubtitleStatus.READY) {
            throw new BadRequestException(ErrorCode.SUBTITLE_INVALID, "Source subtitle must be ready before translation");
        }

        List<String> targetLanguages = normalizeTargetLanguages(request.targetLanguageCodes());
        List<SubtitleJobResponse> responses = new ArrayList<>();
        for (String targetLanguage : targetLanguages) {
            if (targetLanguage.equals(source.getLanguageCode())) {
                throw new BadRequestException(ErrorCode.SUBTITLE_INVALID, "Target language must differ from source language: " + targetLanguage);
            }
            ensureNoExistingSubtitle(lessonId, targetLanguage);
            ensureNoActiveJob(lessonId, targetLanguage);

            SubtitleJob job = newJob(
                    videoLesson,
                    SubtitleJobType.TRANSLATE,
                    source.getId(),
                    source.getLanguageCode(),
                    targetLanguage,
                    null
            );
            SubtitleJob saved = subtitleJobRepository.save(job);
            courseEventProducer.publishSubtitleTranslateRequestedEvent(new SubtitleTranslateRequestedEvent(
                    saved.getId(),
                    courseId,
                    lessonId,
                    videoLesson.getFileKey(),
                    source.getId(),
                    source.getLanguageCode(),
                    source.getFileKey(),
                    source.getFileUrl(),
                    saved.getTargetLanguageCode(),
                    saved.getDisplayName(),
                    saved.getExpectedOutputFileKey()
            ));
            responses.add(subtitleJobMapper.toDto(saved));
        }
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubtitleJobResponse> getJobsByLessonId(String courseId, String lessonId) {
        ensureInstructor(courseId, lessonId);
        return subtitleJobRepository.findByVideoLessonLessonIdOrderByCreatedAtDesc(lessonId)
                .stream()
                .map(subtitleJobMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SubtitleJobResponse getJobById(String courseId, String lessonId, String jobId) {
        ensureInstructor(courseId, lessonId);
        return subtitleJobMapper.toDto(subtitleJobRepository.findByIdAndVideoLessonLessonId(jobId, lessonId)
                .orElseThrow(() -> new NotFoundException("Subtitle job not found with id: " + jobId)));
    }

    @Override
    @Transactional
    public void markProcessing(String jobId, Integer progressPercent) {
        SubtitleJob job = findJob(jobId);
        if (job.getStatus() == SubtitleJobStatus.SUCCEEDED || job.getStatus() == SubtitleJobStatus.FAILED) {
            return;
        }
        job.setStatus(SubtitleJobStatus.PROCESSING);
        job.setProgressPercent(clampProgress(progressPercent));
        subtitleJobRepository.save(job);
    }

    @Override
    @Transactional
    public void markCompleted(SubtitleJobCompletedRequest request) {
        SubtitleJob job = findJob(request.jobId());
        if (job.getStatus() == SubtitleJobStatus.SUCCEEDED && job.getOutputSubtitleId() != null) {
            return;
        }
        if (job.getStatus() == SubtitleJobStatus.FAILED) {
            return;
        }
        try {
            String outputLanguageCode = validateCompletion(job, request);
            ensureNoExistingSubtitle(job.getVideoLesson().getLessonId(), outputLanguageCode);

            SubtitleTrack subtitleTrack = new SubtitleTrack();
            subtitleTrack.setLanguageCode(outputLanguageCode);
            subtitleTrack.setDisplayName(displayName(request.displayName(), displayName(job.getDisplayName(), whisperLanguageRegistry.displayName(outputLanguageCode))));
            subtitleTrack.setFileKey(request.outputFileKey());
            subtitleTrack.setFileUrl(displayName(request.outputFileUrl(), s3Service.publicUrl(request.outputFileKey())));
            subtitleTrack.setOriginalFileKey(null);
            subtitleTrack.setFileName(displayName(request.fileName(), job.getId() + ".vtt"));
            subtitleTrack.setFileType(NORMALIZED_CONTENT_TYPE);
            subtitleTrack.setFileSize(request.fileSize());
            subtitleTrack.setWordConfidenceFileKey(request.wordConfidenceFileKey());
            subtitleTrack.setWordConfidenceFileUrl(request.wordConfidenceFileUrl());
            subtitleTrack.setFormat(SubtitleFormat.VTT);
            subtitleTrack.setSource(job.getJobType() == SubtitleJobType.GENERATE_DEFAULT
                    ? SubtitleSource.AI_GENERATED
                    : SubtitleSource.AI_TRANSLATED);
            subtitleTrack.setStatus(SubtitleStatus.READY);
            subtitleTrack.setIsDefault(job.getJobType() == SubtitleJobType.GENERATE_DEFAULT);
            subtitleTrack.setVideoLesson(job.getVideoLesson());

            SubtitleTrack savedTrack = subtitleTrackRepository.save(subtitleTrack);
            job.setOutputSubtitleId(savedTrack.getId());
            job.setTargetLanguageCode(outputLanguageCode);
            job.setDisplayName(savedTrack.getDisplayName());
            job.setStatus(SubtitleJobStatus.SUCCEEDED);
            job.setProgressPercent(100);
            job.setErrorCode(null);
            job.setErrorMessage(null);
            subtitleJobRepository.save(job);
        } catch (BadRequestException | AlreadyExistException ex) {
            markFailedJob(job, "AI_SUBTITLE_COMPLETION_INVALID", ex.getMessage());
        }
    }

    @Override
    @Transactional
    public void markFailed(String jobId, String errorCode, String errorMessage) {
        SubtitleJob job = findJob(jobId);
        if (job.getStatus() == SubtitleJobStatus.SUCCEEDED) {
            return;
        }
        markFailedJob(job, errorCode, errorMessage);
    }

    private SubtitleJob newJob(
            VideoLesson videoLesson,
            SubtitleJobType jobType,
            String sourceSubtitleId,
            String sourceLanguageCode,
            String targetLanguageCode,
            String displayName
    ) {
        String resolvedDisplayName = AUTO_LANGUAGE_CODE.equals(targetLanguageCode)
                ? displayName(displayName, "Auto-detected")
                : displayName(displayName, whisperLanguageRegistry.displayName(targetLanguageCode));
        SubtitleJob job = new SubtitleJob();
        job.setJobType(jobType);
        job.setStatus(SubtitleJobStatus.QUEUED);
        job.setSourceSubtitleId(sourceSubtitleId);
        job.setSourceLanguageCode(sourceLanguageCode);
        job.setTargetLanguageCode(targetLanguageCode);
        job.setDisplayName(resolvedDisplayName);
        job.setExpectedOutputFileKey(aiSubtitleFileKey(videoLesson.getLessonId(), jobType, targetLanguageCode));
        job.setProgressPercent(0);
        job.setVideoLesson(videoLesson);
        return job;
    }

    private String aiSubtitleFileKey(String lessonId, SubtitleJobType jobType, String targetLanguageCode) {
        String fileName = jobType == SubtitleJobType.GENERATE_DEFAULT
                ? "default"
                : targetLanguageCode;
        return AI_SUBTITLE_STORAGE_PREFIX
                + lessonId + "/"
                + sanitizeStorageSegment(fileName)
                + ".vtt";
    }

    private String sanitizeStorageSegment(String value) {
        return value == null || value.isBlank()
                ? AUTO_LANGUAGE_CODE
                : value.trim().toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9-]+", "-");
    }

    private SubtitleTrack resolveSourceSubtitle(String lessonId, String sourceSubtitleId) {
        if (sourceSubtitleId != null && !sourceSubtitleId.isBlank()) {
            return subtitleTrackRepository.findByIdAndVideoLessonLessonId(sourceSubtitleId, lessonId)
                    .orElseThrow(() -> new NotFoundException("Source subtitle not found with id: " + sourceSubtitleId));
        }
        return subtitleTrackRepository.findByVideoLessonLessonIdAndIsDefaultTrue(lessonId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new BadRequestException(ErrorCode.SUBTITLE_INVALID, "Default subtitle is required before translation"));
    }

    private List<String> normalizeTargetLanguages(List<String> targetLanguageCodes) {
        if (targetLanguageCodes == null || targetLanguageCodes.isEmpty()) {
            throw new BadRequestException(ErrorCode.SUBTITLE_INVALID, "At least one target language is required");
        }
        Set<String> seen = new HashSet<>();
        List<String> normalized = new ArrayList<>();
        for (String targetLanguageCode : targetLanguageCodes) {
            String languageCode = whisperLanguageRegistry.normalize(targetLanguageCode);
            if (!seen.add(languageCode)) {
                throw new BadRequestException(ErrorCode.SUBTITLE_INVALID, "Duplicate target language: " + languageCode);
            }
            normalized.add(languageCode);
        }
        return normalized;
    }

    private String validateCompletion(SubtitleJob job, SubtitleJobCompletedRequest request) {
        if (request.outputFileKey() == null || !request.outputFileKey().equals(job.getExpectedOutputFileKey())) {
            throw new BadRequestException(ErrorCode.SUBTITLE_FILE_INVALID, "AI subtitle output file key does not match the job expectation");
        }
        String languageCode = whisperLanguageRegistry.normalize(request.languageCode());
        if (!AUTO_LANGUAGE_CODE.equals(job.getTargetLanguageCode()) && !languageCode.equals(job.getTargetLanguageCode())) {
            throw new BadRequestException(ErrorCode.SUBTITLE_INVALID, "AI subtitle output language does not match the job target");
        }
        if (request.fileSize() == null || request.fileSize() < 1) {
            throw new BadRequestException(ErrorCode.SUBTITLE_FILE_INVALID, "AI subtitle file size must be greater than 0");
        }
        String fileType = request.fileType() == null ? "" : request.fileType().trim();
        if (!fileType.isBlank() && !NORMALIZED_CONTENT_TYPE.equalsIgnoreCase(fileType)) {
            throw new BadRequestException(ErrorCode.SUBTITLE_FILE_UNSUPPORTED, "AI subtitle output must be text/vtt");
        }
        return languageCode;
    }

    private VideoLesson ensureInstructor(String courseId, String lessonId) {
        lessonService.ensureLessonBelongsToCourse(courseId, lessonId, LessonType.VIDEO);
        VideoLesson videoLesson = ensureVideoLessonExists(lessonId);
        String currentUserId = AuthenticationUtil.extractUserId();
        if (!lessonService.isLessonInstructor(lessonId, currentUserId)) {
            throw new ForbiddenException(ErrorCode.INSTRUCTOR_ACCESS_REQUIRED, "You do not have permission to manage subtitles for this lesson");
        }
        return videoLesson;
    }

    private VideoLesson ensureVideoLessonExists(String lessonId) {
        return videoLessonRepository.findByLessonId(lessonId)
                .orElseThrow(() -> new NotFoundException("Video lesson not found for lessonId: " + lessonId));
    }

    private SubtitleJob findJob(String jobId) {
        return subtitleJobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Subtitle job not found with id: " + jobId));
    }

    private void ensureNoExistingSubtitle(String lessonId, String languageCode) {
        subtitleTrackRepository.findByVideoLessonLessonIdAndLanguageCode(lessonId, languageCode)
                .ifPresent(existing -> {
                    throw new AlreadyExistException(ErrorCode.RESOURCE_ALREADY_EXISTS, "Subtitle already exists for language: " + languageCode);
                });
    }

    private void ensureNoActiveJob(String lessonId, String languageCode) {
        if (subtitleJobRepository.existsByVideoLessonLessonIdAndTargetLanguageCodeAndStatusIn(
                lessonId,
                languageCode,
                List.of(SubtitleJobStatus.QUEUED, SubtitleJobStatus.PROCESSING)
        )) {
            throw new AlreadyExistException(ErrorCode.RESOURCE_ALREADY_EXISTS, "Subtitle AI job already exists for language: " + languageCode);
        }
    }

    private String normalizeOptionalWhisperLanguage(String languageCode) {
        if (languageCode == null || languageCode.isBlank()) {
            return AUTO_LANGUAGE_CODE;
        }
        return whisperLanguageRegistry.normalize(languageCode);
    }

    private int clampProgress(Integer progressPercent) {
        if (progressPercent == null) {
            return 0;
        }
        return Math.max(0, Math.min(99, progressPercent));
    }

    private String displayName(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private void markFailedJob(SubtitleJob job, String errorCode, String errorMessage) {
        job.setStatus(SubtitleJobStatus.FAILED);
        job.setErrorCode(displayName(errorCode, "AI_SUBTITLE_FAILED"));
        job.setErrorMessage(displayName(errorMessage, "AI subtitle job failed"));
        subtitleJobRepository.save(job);
    }
}
