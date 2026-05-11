package com.cinx.social.repository;

import com.cinx.social.model.Report;
import com.cinx.social.model.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, String> {
    Page<Report> findByType(ReportType type, Pageable pageable);
    void deleteByRefIdAndType(String refId, ReportType type);
    List<Report> findAllByRefIdAndType(String refId, ReportType type);
}
