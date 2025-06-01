package org.glue.glue_be.report.repository;

import org.glue.glue_be.report.entity.ReportReason;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportReasonRepository extends JpaRepository<ReportReason, Long> {

    boolean existsByReason(String reason);
}
