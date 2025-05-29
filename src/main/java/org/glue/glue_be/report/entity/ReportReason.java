package org.glue.glue_be.report.entity;

import jakarta.persistence.*;
import lombok.*;
import org.glue.glue_be.common.BaseEntity;

@Entity
@Table(name = "report_reason")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReportReason extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_reason_id")
    private Long reportReasonId;

    @Column(name = "reason", nullable = false, unique = true)
    private String reason;


    @Builder
    public ReportReason(String reason) {
        this.reason = reason;
    }

    public void update(String reason) {
        this.reason = reason;
    }
}
