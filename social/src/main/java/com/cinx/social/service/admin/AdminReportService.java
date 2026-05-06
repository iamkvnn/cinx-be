package com.cinx.social.service.admin;

import com.cinx.common.exception.NotFoundException;
import com.cinx.common.mapper.SortConverter;
import com.cinx.social.model.*;
import com.cinx.social.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminReportService implements IAdminReportService {
    private final ReportRepository reportRepository;
    private final ReviewRepository reviewRepository;
    private final CourseQuestionRepository questionRepository;
    private final CourseAnswerRepository answerRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Report> getReports(ReportType type, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page - 1, size, SortConverter.toSort(sort));
        if (type != null) {
            return reportRepository.findByType(type, pageable);
        }
        return reportRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public void dismissReport(String reportId) {
        reportRepository.deleteById(reportId);
    }

    @Override
    @Transactional
    public void deleteReportedContent(String reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Report not found"));

        switch (report.getType()) {
            case REVIEW -> reviewRepository.deleteById(report.getRefId());
            case QUESTION -> questionRepository.deleteById(report.getRefId());
            case ANSWER -> answerRepository.deleteById(report.getRefId());
        }

        reportRepository.deleteByRefIdAndType(report.getRefId(), report.getType());
    }
}
