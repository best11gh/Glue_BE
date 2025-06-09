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

    // 미팅 ID로 그룹 채팅방 리스트 조회
    List<GroupChatRoom> findByMeeting_MeetingId(Long meetingId);

    // meeting_id fk로 첫번째 그룹 단톡방 가져오기 (지금 서비스 플로우에선 사실상 일대일 관계이긴함)
    Optional<GroupChatRoom> findFirstByMeeting_MeetingId(Long meetingId);

    @Query("SELECT DISTINCT gc FROM GroupChatRoom gc " +
            "JOIN gc.groupUserChatrooms guc " +
            "WHERE guc.user = :user " +
            "ORDER BY gc.updatedAt DESC")
    List<GroupChatRoom> findByUserOrderByGroupChatroomUpdatedAtDesc(@Param("user") User user, Pageable pageable);

    @Query("SELECT DISTINCT gc FROM GroupChatRoom gc " +
            "JOIN gc.groupUserChatrooms guc " +
            "WHERE guc.user = :user " +
            "AND gc.groupChatroomId < :cursorId " +
            "ORDER BY gc.updatedAt DESC")
    List<GroupChatRoom> findByUserAndGroupChatroomUpdatedAtLessThanOrderByGroupChatroomIdDesc(
            @Param("user") User user,
            @Param("cursorId") Long cursorId,
            Pageable pageable);
    boolean existsByMeeting_MeetingId(Long meetingMeetingId);


}