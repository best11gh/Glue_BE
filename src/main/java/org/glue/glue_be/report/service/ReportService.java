package org.glue.glue_be.report.service;


import lombok.RequiredArgsConstructor;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.report.dto.request.CreateReportRequest;
import org.glue.glue_be.report.entity.*;
import org.glue.glue_be.report.repository.*;
import org.glue.glue_be.report.response.ReportResponseStatus;
import org.glue.glue_be.user.entity.User;
import org.glue.glue_be.user.repository.UserRepository;
import org.glue.glue_be.user.response.UserResponseStatus;
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
                .build();

        reportRepository.save(report);
    }

    // TODO: 신고 조회 기능들은 어드민 페이지 확정 난 후 추가 예정
    // 신고 전체 조회
    @Transactional(readOnly = true)
    public void getReports(Long cursorId, Integer pageSize) {
    }

    // 신고 상세 조회
    @Transactional(readOnly = true)
    public void getReport(Long reportId) {
    }

    // 신고 처리 완료로 표시
    public void markReportHandled(Long reportId) {
        Report report = findReport(reportId);

        if (report.isHandled()) {
            throw new BaseException(ReportResponseStatus.ALREADY_HANDLED_REPORT);
        }

        report.markHandled();
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
