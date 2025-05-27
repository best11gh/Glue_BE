package org.glue.glue_be.chat.repository.dm;

import org.glue.glue_be.chat.entity.dm.DmChatRoom;
import org.glue.glue_be.chat.entity.dm.DmUserChatroom;
import org.glue.glue_be.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DmUserChatroomRepository extends JpaRepository<DmUserChatroom, Long> {
    List<DmUserChatroom> findByDmChatRoom(DmChatRoom dmChatRoom);

    void deleteByDmChatRoomAndUser(DmChatRoom dmChatRoom, User user);

    Optional<DmUserChatroom> findByDmChatRoomAndUser(DmChatRoom dmChatRoom, User sender);

    @Query(value = "SELECT DISTINCT dc.* FROM dm_chatroom dc " +
            "JOIN dm_user_chatroom duc ON dc.dm_chatroom_id = duc.dm_chatroom_id " +
            "WHERE duc.user_id = :userId " +
            "ORDER BY dc.dm_chatroom_id DESC",
            nativeQuery = true)
    List<DmChatRoom> findDmChatRoomsByUserOrderByDmChatRoomIdDesc(@Param("userId") Long userId, Pageable pageable);

    @Query(value = "SELECT DISTINCT dc.* FROM dm_chatroom dc " +
            "JOIN dm_user_chatroom duc ON dc.dm_chatroom_id = duc.dm_chatroom_id " +
            "WHERE duc.user_id = :userId AND dc.dm_chatroom_id < :cursorId " +
            "ORDER BY dc.dm_chatroom_id DESC",
            nativeQuery = true)
    List<DmChatRoom> findDmChatRoomsByUserAndDmChatRoomIdLessThanOrderByDmChatRoomIdDesc(@Param("userId") Long userId, @Param("cursorId") Long cursorId, Pageable pageable);

    Optional<DmUserChatroom> findByUser_UserIdAndDmChatRoom_Id(Long userId, Long dmChatRoomId);

    @Modifying
    @Query("UPDATE DmUserChatroom du SET du.lastReadMessageId = :messageId " +
            "WHERE du.user.userId = :userId AND du.dmChatRoom.id = :dmChatroomId")
    void updateLastReadMessageId(@Param("userId") Long userId,
                                 @Param("dmChatroomId") Long dmChatroomId,
                                 @Param("messageId") Long messageId);
}