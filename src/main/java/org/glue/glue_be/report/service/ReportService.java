package org.glue.glue_be.report.service;


import java.util.List;
import lombok.RequiredArgsConstructor;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.report.dto.request.CreateReportRequest;
import org.glue.glue_be.report.dto.request.ReportHandledRequest;
import org.glue.glue_be.report.dto.response.ReportResponse;
import org.glue.glue_be.report.entity.*;
import org.glue.glue_be.report.repository.*;
import org.glue.glue_be.report.response.ReportResponseStatus;
import org.glue.glue_be.user.entity.User;
import org.glue.glue_be.user.repository.UserRepository;
import org.glue.glue_be.user.response.UserResponseStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ReportReasonRepository reportReasonRepository;

    // 신고하기
    public void create(Long reporterId, CreateReportRequest request) {
        User reporter = findUser(reporterId);
        User reported = findUser(request.reportedId());
        ReportReason reason = findReason(request.reasonId());

        Report report = Report.builder()
                .reporter(reporter)
                .reported(reported)
                .reason(reason)
                .reportRoute(request.reportRoute())
                .detail(request.detail())
                .build();

        reportRepository.save(report);
    }

    // 신고 전체 조회
    // 신고 사유, 조치 상태, keyword로 검색 가능
    @Transactional(readOnly = true)
    public List<ReportResponse> getReports(Long cursorId, Integer pageSize, Integer reasonId, Boolean handled,
                                           String keyword) {

        Pageable pageable = PageRequest.of(0, pageSize + 1);
        List<Report> reports = reportRepository.findReportsWithFilters(cursorId, reasonId, handled, keyword, pageable);

        boolean hasNext = reports.size() > pageSize;
        if (hasNext) {
            reports = reports.subList(0, pageSize);
        }

        return reports.stream()
                .map(ReportResponse::from)
                .toList();
    }

    // 신고 상세 조회
    @Transactional(readOnly = true)
    public ReportResponse getReport(Long reportId) {
        Report report = findReport(reportId);
        return ReportResponse.from(report);
    }

    // 신고 처리
    public void handleReport(Long reportId, ReportHandledRequest request) {
        Report report = findReport(reportId);

        if (request.accept()) {
            report.accept();

            User reportedUser = report.getReported();
            reportedUser.increaseAcceptedReportCount();
        } else {
            report.reject();
        }
    }


    // 신고 삭제
    public void delete(Long reportId) {
        Report report = findReport(reportId);
        reportRepository.delete(report);
    }


    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(UserResponseStatus.USER_NOT_FOUND));
    }

    private ReportReason findReason(Long reasonId) {
        return reportReasonRepository.findById(reasonId)
                .orElseThrow(() -> new BaseException(ReportResponseStatus.REPORT_REASON_NOT_FOUND));
    }

    private Report findReport(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new BaseException(ReportResponseStatus.REPORT_NOT_FOUND));
    }
}
