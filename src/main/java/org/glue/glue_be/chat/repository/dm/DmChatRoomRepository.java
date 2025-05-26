package org.glue.glue_be.chat.repository.dm;

import org.glue.glue_be.chat.entity.dm.DmChatRoom;
import org.glue.glue_be.meeting.entity.Meeting;
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

    // 호스트 기준 첫 페이지 조회
    List<DmChatRoom> findByMeetingHostOrderByIdDesc(User host, Pageable pageable);

    // 호스트 기준 커서 조회
    List<DmChatRoom> findByMeetingHostAndIdLessThanOrderByIdDesc(User host, Long cursorId, Pageable pageable);
}