package org.glue.glue_be.notice.repository;

import java.util.List;
import org.glue.glue_be.notice.entity.Notice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findAllByOrderByNoticeIdDesc(Pageable pageable);

    List<Notice> findByNoticeIdLessThanOrderByNoticeIdDesc(Long cursorId, Pageable pageable);

}
