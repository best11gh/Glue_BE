package org.glue.glue_be.chat.repository.group;

import org.glue.glue_be.chat.entity.group.GroupChatRoom;
import org.glue.glue_be.chat.entity.group.GroupMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupMessageRepository extends JpaRepository<GroupMessage, Long> {

    // 채팅방 ID로 메시지 조회 (최신순)
    List<GroupMessage> findByGroupChatroomOrderByCreatedAtAsc(GroupChatRoom groupChatroom);

    // 채팅방의 가장 최근 메시지 조회
    Optional<GroupMessage> findTopByGroupChatroomOrderByCreatedAtDesc(GroupChatRoom groupChatroom);

    // 읽지 않은 메시지 존재 여부 확인
    boolean existsByGroupChatroomAndUser_UserIdNotAndUnreadCount(GroupChatRoom groupChatroom, Long userId, Integer isRead);

    // 읽지 않은 메시지 조회
    @Query("SELECT gm FROM GroupMessage gm WHERE gm.groupChatroom.groupChatroomId = :groupChatroomId AND gm.user.userId != :userId AND gm.unreadCount != 0")
    List<GroupMessage> findUnreadMessages(@Param("groupChatroomId") Long groupChatroomId, @Param("userId") Long userId);
}