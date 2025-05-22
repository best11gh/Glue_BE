package org.glue.glue_be.chat.repository.group;

import org.glue.glue_be.chat.entity.group.GroupChatRoom;
import org.glue.glue_be.meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GroupChatRoomRepository extends JpaRepository<GroupChatRoom, Long> {

    // 미팅 ID로 그룹 채팅방 조회
    Optional<GroupChatRoom> findByMeeting_MeetingId(Long meetingId);

    // 미팅 목록으로 그룹 채팅방 목록 조회
    List<GroupChatRoom> findByMeetingIn(List<Meeting> meetings);

    // 특정 사용자가 참여한 그룹 채팅방 조회
    @Query("SELECT gc FROM GroupChatRoom gc JOIN gc.groupUserChatrooms guc WHERE guc.user.userId = :userId")
    List<GroupChatRoom> findByUserId(@Param("userId") Long userId);

    @Query("SELECT g FROM GroupChatRoom g JOIN g.meeting m WHERE m.meetingTime < :threshold")
    List<GroupChatRoom> findByMeetingTime(@Param("threshold") LocalDateTime threshold);
}