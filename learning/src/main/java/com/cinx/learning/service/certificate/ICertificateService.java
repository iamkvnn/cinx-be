package com.cinx.learning.service.certificate;

import com.cinx.learning.consts.CertificateStatus;
import com.cinx.learning.dto.response.CertificateRequestResponse;
import org.springframework.data.domain.Page;
import java.util.List;

public interface ICertificateService {
    CertificateRequestResponse applyForCertificate(String userId, String courseId);
    Page<CertificateRequestResponse> getRequestsByCourse(String currentUserId, String courseId, CertificateStatus status, int page, int size);
    Page<CertificateRequestResponse> getAllRequests(CertificateStatus status, int page, int size);
    CertificateRequestResponse approveCertificate(String currentUserId, String requestId);
    CertificateRequestResponse rejectCertificate(String currentUserId, String requestId);
    CertificateRequestResponse getCertificate(String userId, String courseId);
    List<CertificateRequestResponse> getMyCertificates(String userId);
}
