package org.glue.glue_be.chat.repository.dm;

import org.glue.glue_be.chat.entity.dm.DmChatRoom;
import org.glue.glue_be.chat.entity.dm.DmMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

@Repository
public interface DmMessageRepository extends JpaRepository<DmMessage, Long> {

    List<DmMessage> findByDmChatRoomOrderByCreatedAtAsc(DmChatRoom dmChatRoom);

    Optional<DmMessage> findTopByDmChatRoomOrderByCreatedAtDesc(DmChatRoom chatRoom);

    Optional<DmMessage> findTopByDmChatRoomIdOrderByCreatedAtDesc(Long dmChatRoomId);

    // 커서 기반 메시지 조회를 위한 새로운 메서드들 (id 필드 사용)
    // 첫 번째 요청 (cursorId가 null일 때) - 최신 메시지부터 내림차순
    List<DmMessage> findByDmChatRoomOrderByIdDesc(DmChatRoom dmChatRoom, Pageable pageable);

    // 두 번째 요청부터 (cursorId가 있을 때) - 해당 ID보다 작은 값들을 내림차순으로
    List<DmMessage> findByDmChatRoomAndIdLessThanOrderByIdDesc(
            DmChatRoom dmChatRoom,
            Long cursorId,
            Pageable pageable);
}