package org.glue.glue_be.chat.repository.group;

import org.glue.glue_be.chat.entity.group.GroupChatRoom;
import org.glue.glue_be.chat.entity.group.GroupUserChatRoom;
import org.glue.glue_be.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupUserChatRoomRepository extends JpaRepository<GroupUserChatRoom, Long> {

    // 채팅방과 유저로 조회
    Optional<GroupUserChatRoom> findByGroupChatroomAndUser(GroupChatRoom groupChatroom, User user);

    // 채팅방의 모든 참여자 조회
    List<GroupUserChatRoom> findByGroupChatroom(GroupChatRoom groupChatroom);

    // 유저가 참여한 모든 채팅방 조회
    List<GroupUserChatRoom> findByUser(User user);

    // 채팅방과 유저로 삭제
    void deleteByGroupChatroomAndUser(GroupChatRoom groupChatroom, User user);

	void deleteByGroupChatroom_GroupChatroomId(Long roomId);

}