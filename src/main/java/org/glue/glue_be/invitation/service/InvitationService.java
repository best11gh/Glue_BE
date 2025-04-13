package org.glue.glue_be.invitation.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.common.response.BaseResponseStatus;
import org.glue.glue_be.invitation.dto.InvitationDto;
import org.glue.glue_be.invitation.entity.Invitation;
import org.glue.glue_be.invitation.repository.InvitationRepository;
import org.glue.glue_be.invitation.response.InvitationResponseStatus;
import org.glue.glue_be.meeting.entity.Meeting;
import org.glue.glue_be.meeting.entity.Participant;
import org.glue.glue_be.meeting.repository.MeetingRepository;
import org.glue.glue_be.meeting.repository.ParticipantRepository;
import org.glue.glue_be.meeting.response.MeetingResponseStatus;
import org.glue.glue_be.user.entity.User;
import org.glue.glue_be.user.repository.UserRepository;
import org.glue.glue_be.user.response.UserResponseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final MeetingRepository meetingRepository;
    private final ParticipantRepository participantRepository;
    
    /**
     * 미팅 초대장 생성
     */
    @Transactional
    public InvitationDto.Response createInvitation(InvitationDto.CreateRequest request, Long creatorId) {
        // 사용자 조회
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new BaseException(UserResponseStatus.USER_NOT_FOUND));
        
        // 미팅 존재 확인 
        if (request.getMeetingId() == null) {
            throw new BaseException(BaseResponseStatus.INVALID_INPUT_VALUE, "미팅 ID는 필수입니다");
        }
        
        Meeting meeting = meetingRepository.findById(request.getMeetingId())
                .orElseThrow(() -> new BaseException(MeetingResponseStatus.MEETING_NOT_FOUND));
        
        // inviteeId가 있는 경우 해당 사용자가 유효한지 확인
        if (request.getInviteeId() != null) {
            userRepository.findById(request.getInviteeId())
                    .orElseThrow(() -> new BaseException(UserResponseStatus.USER_NOT_FOUND, "초대할 사용자를 찾을 수 없습니다"));
        }
        
        String code = generateUniqueCode();
        
        // expiresAt 계산
        LocalDateTime expiresAt = LocalDateTime.now();
        
        if (request.getExpirationDays() != null && request.getExpirationDays() > 0) {
            expiresAt = expiresAt.plusDays(request.getExpirationDays());
        }
        
        if (request.getExpirationHours() != null && request.getExpirationHours() > 0) {
            expiresAt = expiresAt.plusHours(request.getExpirationHours());
        }
        
        // 기본값: 기본 만료 시간이 설정되지 않은 경우 24시간으로 설정
        if (request.getExpirationDays() == null && request.getExpirationHours() == null) {
            expiresAt = expiresAt.plusDays(1);
        }
        
        Invitation invitation = Invitation.builder()
                .code(code)
                .expiresAt(expiresAt)
                .maxUses(request.getMaxUses())
                .creator(creator)
                .meetingId(request.getMeetingId())
                .inviteeId(request.getInviteeId())
                .build();
        
        return InvitationDto.Response.from(invitationRepository.save(invitation));
    }
    
    /**
     * 미팅 초대장 수락
     */
    @Transactional
    public void acceptInvitation(String code, Long userId) {
        // 비관적 락을 사용하여 동시성 제어
        Invitation invitation = invitationRepository.findByCodeWithLock(code)
                .orElseThrow(() -> new BaseException(InvitationResponseStatus.INVITATION_NOT_FOUND));
        
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(UserResponseStatus.USER_NOT_FOUND));
        
        // 초대장 유효성 검사
        if (!invitation.isValid()) {
            if (LocalDateTime.now().isAfter(invitation.getExpiresAt())) {
                throw new BaseException(InvitationResponseStatus.INVITATION_EXPIRED);
            } else if (invitation.getUsedCount() >= invitation.getMaxUses()) {
                throw new BaseException(InvitationResponseStatus.INVITATION_FULLY_USED);
            } else {
                throw new BaseException(InvitationResponseStatus.INVITATION_INVALID);
            }
        }
        
        // 특정 사용자를 위한 초대장인 경우, 해당 사용자만 사용 가능하도록 체크
        if (!invitation.canBeUsedBy(userId)) {
            throw new BaseException(InvitationResponseStatus.INVITATION_NOT_FOR_USER);
        }
        
        // 초대장 사용 횟수 증가
        invitation.incrementUsedCount();
        
        // 미팅 처리
        Meeting meeting = meetingRepository.findById(invitation.getMeetingId())
                .orElseThrow(() -> new BaseException(MeetingResponseStatus.MEETING_NOT_FOUND));
        
        // 미팅이 꽉 찬 경우 체크
        if (meeting.isMeetingFull()) {
            throw new BaseException(MeetingResponseStatus.MEETING_FULL);
        }
        
        // 이미 참여한 사용자인지 체크
        if (participantRepository.existsByUserAndMeeting(user, meeting)) {
            throw new BaseException(InvitationResponseStatus.INVITATION_ALREADY_JOINED);  // 이미 참여 중
        }
        
        // 참가자 추가
        Participant participant = Participant.builder()
                .user(user)
                .meeting(meeting)
                .build();
        
        participantRepository.save(participant);
        meeting.addParticipant(participant);
        
        // 현재 참여자 수 업데이트
        meeting.changeStatus(1);  // 1: 활성화
    }
    
    /**
     * 초대장 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<InvitationDto.Response> getInvitations(Long creatorId, Pageable pageable) {
        // 사용자 조회
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new BaseException(UserResponseStatus.USER_NOT_FOUND));
        
        return invitationRepository.findByCreator(creator, pageable)
                .map(InvitationDto.Response::from);
    }
    
    /**
     * 고유한 초대 코드 생성
     */
    private String generateUniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
} 