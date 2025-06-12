package org.glue.glue_be.chat.repository.group;

import org.glue.glue_be.chat.entity.dm.DmUserChatroom;
import org.glue.glue_be.chat.entity.group.GroupChatRoom;
import org.glue.glue_be.chat.entity.group.GroupUserChatRoom;
import org.glue.glue_be.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupUserChatRoomRepository extends JpaRepository<GroupUserChatRoom, Long> {

    // 채팅방과 유저로 조회
    Optional<GroupUserChatRoom> findByGroupChatroomAndUser(GroupChatRoom groupChatroom, User user);

    // 채팅방의 모든 참여자 조회
    List<GroupUserChatRoom> findByGroupChatroom(GroupChatRoom groupChatroom);

    List<GroupUserChatRoom> findByGroupChatroom_GroupChatroomId(Long Id);

    // 채팅방과 유저로 삭제
    void deleteByGroupChatroomAndUser(GroupChatRoom groupChatroom, User user);


	void deleteByGroupChatroom_GroupChatroomId(Long roomId);

    Optional<GroupUserChatRoom> findByUser_UserIdAndGroupChatroom_GroupChatroomId(Long userId, Long groupChatroomId);

    @Modifying
    @Query("UPDATE GroupUserChatRoom g SET g.lastReadMessageId = :messageId " +
            "WHERE g.user.userId = :userId AND g.groupChatroom.groupChatroomId = :groupChatroomId")
    void updateLastReadMessageId(@Param("userId") Long userId,
                                 @Param("groupChatroomId") Long groupChatroomId,
                                 @Param("messageId") Long messageId);

}