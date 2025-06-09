package org.glue.glue_be.chat.repository.dm;

import org.glue.glue_be.chat.entity.dm.DmChatRoom;
import org.glue.glue_be.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface DmChatRoomRepository extends JpaRepository<DmChatRoom, Long> {

    // 두 사용자 간 1:1 채팅방 조회
    @Query("SELECT cr FROM DmChatRoom cr " +
            "JOIN cr.dmUserChatrooms uc1 " +
            "JOIN cr.dmUserChatrooms uc2 " +
            "WHERE uc1.user.userId = :userId1 AND uc2.user.userId = :userId2 AND cr.meeting.meetingId = :meetingId " +
            "AND (SELECT COUNT(uc) FROM DmUserChatroom uc WHERE uc.dmChatRoom = cr) = 2")
    Optional<DmChatRoom> findDirectChatRoomByUserIds(
            @Param("meetingId") Long meetingId,
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2);

	List<DmChatRoom> findByMeeting_MeetingId(Long meetingMeetingId);

    // 첫 페이지 조회
    List<DmChatRoom> findByMeetingHostOrderByUpdatedAtDesc(User host, Pageable pageable);

    // 커서 기반 조회 - LocalDateTime 파라미터 사용
    @Query("SELECT dcr FROM DmChatRoom dcr WHERE dcr.meeting.host = :host AND dcr.id < :cursorId ORDER BY dcr.updatedAt DESC")
    List<DmChatRoom> findByMeetingHostAndUpdatedAtLessThanOrderByUpdatedAtDesc(
            @Param("host") User host,
            @Param("cursorId") Long cursorId,
            Pageable pageable);

}