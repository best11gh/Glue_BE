package org.glue.glue_be.chat.repository.dm;

import org.glue.glue_be.chat.entity.dm.DmChatRoom;
import org.glue.glue_be.chat.entity.dm.DmMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DmMessageRepository extends JpaRepository<DmMessage, Long> {

    List<DmMessage> findByDmChatRoomOrderByCreatedAtAsc(DmChatRoom dmChatRoom);

    // 채팅방의 가장 최근 메시지 조회
    @Query("SELECT m FROM DmMessage m " +
            "WHERE m.dmChatRoom.id = :dmChatRoomId " +
            "ORDER BY m.createdAt DESC")
    List<DmMessage> findRecentByDmChatRoomId(@Param("dmChatRoomId") Long dmChatRoomId);

    // 특정 시간 이후의 메시지 개수 조회 (읽지 않은 메시지 수 계산용)
    @Query("SELECT COUNT(m) FROM DmMessage m " +
            "WHERE m.dmChatRoom.id = :dmChatRoomId " +
            "AND m.createdAt > :lastReadTime")
    int countUnreadMessages(
            @Param("dmChatRoomId") Long dmChatRoomId,
            @Param("lastReadTime") LocalDateTime lastReadTime);
    Object countByDmChatRoomIdAndIsReadAndUserUserIdNot(Long dmChatRoomId, int b, Long userId);


    Optional<DmMessage> findTopByDmChatRoomOrderByCreatedAtDesc(DmChatRoom chatRoom);

    boolean existsByDmChatRoomAndUser_UserIdNotAndIsRead(DmChatRoom dmChatRoom, Long userId, int isRead);

    @Query("SELECT m FROM DmMessage m WHERE m.dmChatRoom.id = :chatRoomId AND m.user.userId != :userId AND m.isRead = 0")
    List<DmMessage> findUnreadMessages(@Param("chatRoomId") Long chatRoomId, @Param("userId") Long userId);
}