package org.glue.glue_be.report.repository;

import java.util.List;
import org.glue.glue_be.report.entity.Report;
import org.glue.glue_be.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {


    @Query("""
                SELECT r FROM Report r
                JOIN FETCH r.reported
                JOIN FETCH r.reason
                WHERE (:reasonId IS NULL OR r.reason.reportReasonId = :reasonId)
                  AND (:handled IS NULL OR
                       (:handled = TRUE AND r.accepted IS NOT NULL) OR
                       (:handled = FALSE AND r.accepted IS NULL))
                  AND (:keyword IS NULL OR
                              LOWER(r.detail) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                              LOWER(r.reported.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
                  AND (:cursorId IS NULL OR r.reportId < :cursorId)
                ORDER BY r.createdAt DESC
            """)
    List<Report> findReportsWithFilters(
            @Param("cursorId") Long cursorId,
            @Param("reasonId") Integer reasonId,
            @Param("handled") Boolean handled,
            @Param("keyword") String keyword,
            Pageable pageable
    );


    List<Report> findByReportedAndAcceptedTrue(User reported);

}
