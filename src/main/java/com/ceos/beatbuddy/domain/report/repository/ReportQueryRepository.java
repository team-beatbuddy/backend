package com.ceos.beatbuddy.domain.report.repository;

import com.ceos.beatbuddy.domain.report.entity.Report;

import java.util.List;

public interface ReportQueryRepository {
    List<Report> getAllReports();
}
