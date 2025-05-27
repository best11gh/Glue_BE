package org.glue.glue_be.chat.repository.group;

import org.glue.glue_be.chat.entity.group.GroupChatRoom;
import org.glue.glue_be.chat.entity.group.GroupMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

public interface GroupMessageRepository extends JpaRepository<GroupMessage, Long> {

    // 채팅방 ID로 메시지 조회 (최신순)
    List<GroupMessage> findByGroupChatroomOrderByCreatedAtAsc(GroupChatRoom groupChatroom);

    // 채팅방의 가장 최근 메시지 조회
    Optional<GroupMessage> findTopByGroupChatroomOrderByCreatedAtDesc(GroupChatRoom groupChatroom);

    // 커서 기반 메시지 조회를 위한 새로운 메서드들
    // 첫 번째 요청 (cursorId가 null일 때) - 최신 메시지부터 내림차순
    List<GroupMessage> findByGroupChatroomOrderByGroupMessageIdDesc(GroupChatRoom groupChatroom, Pageable pageable);

    // 두 번째 요청부터 (cursorId가 있을 때) - 해당 ID보다 작은 값들을 내림차순으로
    List<GroupMessage> findByGroupChatroomAndGroupMessageIdLessThanOrderByGroupMessageIdDesc(
            GroupChatRoom groupChatroom,
            Long cursorId,
            Pageable pageable);
}