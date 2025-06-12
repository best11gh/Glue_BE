package org.glue.glue_be.report.entity;

import jakarta.persistence.*;
import lombok.*;
import org.glue.glue_be.common.BaseEntity;
import org.glue.glue_be.user.entity.User;

@Entity
@Table(name = "report")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_id", nullable = false)
    private User reported;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reason_id", nullable = false)
    private ReportReason reason;

    // 0: 모임글, 1: 채팅, 2: 방명록
    @Column(name = "report_route", nullable = false)
    private Integer reportRoute;

    @Column(name = "detail", length = 500)
    private String detail;

    @Column(name = "accepted")
    private Boolean accepted;

    public void accept() {
        this.accepted = true;
    }

    public void reject() {
        this.accepted = false;
    }

    @Builder
    public Report(User reporter, User reported, ReportReason reason, Integer reportRoute, String detail) {
        this.reporter = reporter;
        this.reported = reported;
        this.reason = reason;
        this.reportRoute = reportRoute;
        this.detail = detail;
        this.accepted = null;
    }
}
