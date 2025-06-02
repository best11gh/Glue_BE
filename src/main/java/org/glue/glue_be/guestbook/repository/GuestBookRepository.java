package org.glue.glue_be.guestbook.repository;

import java.util.List;
import org.glue.glue_be.guestbook.entity.GuestBook;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuestBookRepository extends JpaRepository<GuestBook, Long> {
    // 방명록만 조회
    List<GuestBook> findByHost_UserIdAndParentIsNullOrderByGuestBookIdDescCreatedAtDesc(
            Long hostId,
            Pageable pageable
    );

    //  cursorId 미만의 ID를 가진 방명록 조회
    List<GuestBook> findByHost_UserIdAndParentIsNullAndGuestBookIdLessThanOrderByGuestBookIdDescCreatedAtDesc(
            Long hostId,
            Long cursorId,
            Pageable pageable
    );

    // 특정 방명록에 달린 댓글 조회
    List<GuestBook> findByParent_GuestBookIdInOrderByCreatedAtAsc(
            List<Long> parentIds
    );

    Long countByHost_UserId(Long userId);

    void deleteByHost_UserId(Long userId);
}
