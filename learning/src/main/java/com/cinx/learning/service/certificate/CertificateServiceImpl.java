package com.cinx.learning.service.certificate;

import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.ErrorCode;
import com.cinx.common.exception.NotFoundException;
import com.cinx.learning.consts.CertificateStatus;
import com.cinx.learning.dto.response.CertificateRequestResponse;
import com.cinx.learning.dto.response.CourseResponse;
import com.cinx.learning.dto.response.CourseProgressResponse;
import com.cinx.learning.dto.response.UserDto;
import com.cinx.learning.mapper.CertificateRequestMapper;
import com.cinx.learning.model.CertificateRequest;
import com.cinx.learning.repository.CertificateRequestRepository;
import com.cinx.learning.service.course.CourseService;
import com.cinx.learning.service.learningProgress.ILearningProgressService;
import com.cinx.learning.service.s3.S3Service;
import com.cinx.learning.service.user.UserService;
import com.cinx.learning.service.authorization.LearningAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements ICertificateService {

    private final CertificateRequestRepository certificateRequestRepository;
    private final CertificateRequestMapper certificateRequestMapper;
    private final CourseService courseService;
    private final UserService userService;
    private final ILearningProgressService learningProgressService;
    private final CertificateGeneratorService certificateGeneratorService;
    private final S3Service s3Service;
    private final LearningAuthorizationService authorizationService;

    @Override
    @Transactional
    public CertificateRequestResponse applyForCertificate(String userId, String courseId) {
        if (certificateRequestRepository.findByUserIdAndCourseId(userId, courseId).isPresent()) {
            throw new BadRequestException(ErrorCode.CERTIFICATE_ALREADY_APPLIED, "You have already applied for a certificate for this course");
        }

        CourseResponse course = courseService.getCourseById(courseId).data();
        if (Boolean.FALSE.equals(course.hasCertificate())) {
            throw new BadRequestException(ErrorCode.CERTIFICATE_NOT_AVAILABLE, "This course does not offer a certificate");
        }

        CourseProgressResponse progress = learningProgressService.getCourseProgress(userId, courseId);
        if (progress == null || !progress.isCompleted() || !Boolean.TRUE.equals(progress.isPassed())) {
            throw new BadRequestException(ErrorCode.COURSE_NOT_COMPLETED, "You have not completed this course yet");
        }

        CertificateRequest request = CertificateRequest.builder()
                .userId(userId)
                .courseId(courseId)
                .status(CertificateStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();
        return certificateRequestMapper.toDto(certificateRequestRepository.save(request));
    }

    @Override
    public Page<CertificateRequestResponse> getRequestsByCourse(String currentUserId, String courseId, CertificateStatus status, int page, int size) {
        authorizationService.requireCourseInstructor(currentUserId, courseId);
        if (status != null) {
            return certificateRequestRepository.findByCourseIdAndStatus(courseId, status, PageRequest.of(page - 1, size))
                    .map(certificateRequestMapper::toDto);
        }
        return certificateRequestRepository.findByCourseId(courseId, PageRequest.of(page - 1, size))
                .map(certificateRequestMapper::toDto);
    }

    @Override
    public Page<CertificateRequestResponse> getAllRequests(CertificateStatus status, int page, int size) {
        if (status != null) {
            return certificateRequestRepository.findByStatus(status, PageRequest.of(page - 1, size))
                    .map(certificateRequestMapper::toDto);
        }
        return certificateRequestRepository.findAll(PageRequest.of(page - 1, size))
                .map(certificateRequestMapper::toDto);
    }

    @Override
    @Transactional
    public CertificateRequestResponse approveCertificate(String currentUserId, String requestId) {
        CertificateRequest request = certificateRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Certificate request not found"));
        authorizationService.requireCourseInstructor(currentUserId, request.getCourseId());

        if (request.getStatus() != CertificateStatus.PENDING) {
            throw new BadRequestException(ErrorCode.CERTIFICATE_REQUEST_NOT_PENDING, "Request is not in PENDING state");
        }

        CourseResponse courseDto = courseService.getCourseById(request.getCourseId()).data();
        
        UserDto userDto = userService.getUserById(request.getUserId()).data();
        String title = courseDto.certificateTitle() != null ? courseDto.certificateTitle() : "Certificate of Completion";

        byte[] certificateImage = certificateGeneratorService.generateCertificate(
                userDto.name(),
                courseDto.title(),
                title
        );

        String objectKey = "certificates/" + request.getCourseId() + "/" + UUID.randomUUID().toString() + ".png";
        String uploadedUrl = s3Service.uploadFile(objectKey, certificateImage, "image/png");

        request.setStatus(CertificateStatus.APPROVED);
        request.setCertificateUrl(uploadedUrl);
        request.setApprovedAt(LocalDateTime.now());

        return certificateRequestMapper.toDto(certificateRequestRepository.save(request));
    }

    @Override
    @Transactional
    public CertificateRequestResponse rejectCertificate(String currentUserId, String requestId) {
        CertificateRequest request = certificateRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Certificate request not found"));
        authorizationService.requireCourseInstructor(currentUserId, request.getCourseId());

        if (request.getStatus() != CertificateStatus.PENDING) {
            throw new BadRequestException(ErrorCode.CERTIFICATE_REQUEST_NOT_PENDING, "Request is not in PENDING state");
        }

        request.setStatus(CertificateStatus.REJECTED);
        return certificateRequestMapper.toDto(certificateRequestRepository.save(request));
    }

    @Override
    public CertificateRequestResponse getCertificate(String userId, String courseId) {
        return certificateRequestRepository.findByUserIdAndCourseId(userId, courseId)
                .map(certificateRequestMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Certificate not found for this user and course"));
    }

    @Override
    public List<CertificateRequestResponse> getMyCertificates(String userId) {
        return certificateRequestRepository.findByUserIdAndStatus(userId, CertificateStatus.APPROVED)
                .stream()
                .map(certificateRequestMapper::toDto)
                .toList();
    }
}
