package org.glue.glue_be.chat.repository.group;

import org.glue.glue_be.chat.entity.group.GroupChatRoom;
import org.glue.glue_be.meeting.entity.Meeting;
import org.glue.glue_be.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;


public interface GroupChatRoomRepository extends JpaRepository<GroupChatRoom, Long> {

    // 미팅 ID로 그룹 채팅방 조회
    Optional<GroupChatRoom> findByMeeting_MeetingId(Long meetingId);


    // GroupChatRoomRepository에서 메서드 수정
    @Query("SELECT DISTINCT gc FROM GroupChatRoom gc " +
            "JOIN gc.groupUserChatrooms guc " +
            "WHERE guc.user = :user " +
            "ORDER BY gc.groupChatroomId DESC")
    List<GroupChatRoom> findByUserOrderByGroupChatroomIdDesc(@Param("user") User user, Pageable pageable);

    @Query("SELECT DISTINCT gc FROM GroupChatRoom gc " +
            "JOIN gc.groupUserChatrooms guc " +
            "WHERE guc.user = :user " +
            "AND gc.groupChatroomId < :cursorId " +
            "ORDER BY gc.groupChatroomId DESC")
    List<GroupChatRoom> findByUserAndGroupChatroomIdLessThanOrderByGroupChatroomIdDesc(
            @Param("user") User user,
            @Param("cursorId") Long cursorId,
            Pageable pageable);
}