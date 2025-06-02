package org.glue.glue_be.invitation.repository;

import jakarta.persistence.LockModeType;
import org.glue.glue_be.invitation.entity.Invitation;
import org.glue.glue_be.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    
    Optional<Invitation> findByCode(String code);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Invitation i WHERE i.code = :code")
    Optional<Invitation> findByCodeWithLock(@Param("code") String code);
    
    Page<Invitation> findByCreator(User creator, Pageable pageable);

    // 미팅과 참여자 기반으로 초대 status 조회 (쪽지에 필요)
    @Query("SELECT i.status FROM Invitation i " +
            "JOIN i.meeting m " +
            "JOIN m.participants p " +
            "JOIN i.creator c " +
            "JOIN p.user u " +
            "WHERE m.meetingId = :meetingId " +
            "AND c.userId = :creatorUserId " +
            "AND u.userId = :inviteeUserId")
    Integer findStatusByMeetingAndParticipantIds(
            @Param("meetingId") Long meetingId,
            @Param("creatorUserId") Long creatorUserId,
            @Param("inviteeUserId") Long inviteeUserId);

	void deleteByMeeting_MeetingId(Long meetingMeetingId);

}