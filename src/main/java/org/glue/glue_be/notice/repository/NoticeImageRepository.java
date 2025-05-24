package org.glue.glue_be.notice.repository;

import org.glue.glue_be.notice.entity.NoticeImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeImageRepository extends JpaRepository<NoticeImage, Long> {
}
