package org.glue.glue_be.invitation.service;

import lombok.RequiredArgsConstructor;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.invitation.dto.InvitationDto;
import org.glue.glue_be.invitation.entity.Invitation;
import org.glue.glue_be.invitation.repository.InvitationRepository;
import org.glue.glue_be.invitation.response.InvitationResponseStatus;
import org.glue.glue_be.meeting.entity.Meeting;
import org.glue.glue_be.meeting.repository.*;
import org.glue.glue_be.meeting.response.MeetingResponseStatus;
import org.glue.glue_be.meeting.service.MeetingService;
import org.glue.glue_be.user.entity.User;
import org.glue.glue_be.user.repository.UserRepository;
import org.glue.glue_be.user.response.UserResponseStatus;
import org.springframework.data.domain.*;
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
    private final MeetingService meetingService;

    //미팅 초대장 생성

    @Transactional
    public InvitationDto.Response createInvitation(InvitationDto.CreateRequest request, Long creatorId) {
        // 사용자 조회
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new BaseException(UserResponseStatus.USER_NOT_FOUND));

        Meeting meeting = meetingRepository.findById(request.getMeetingId())
                .orElseThrow(() -> new BaseException(MeetingResponseStatus.MEETING_NOT_FOUND));

        // 사용자가 해당 모임의 참가자인지 확인
        if (!participantRepository.existsByUserAndMeeting(creator, meeting)) {
            throw new BaseException(MeetingResponseStatus.NOT_JOINED);
        }

        // inviteeId가 있는 경우 해당 사용자가 유효한지 확인
        if (request.getInviteeId() != null) {
            userRepository.findById(request.getInviteeId())
                    .orElseThrow(() -> new BaseException(UserResponseStatus.USER_NOT_FOUND, "초대할 사용자를 찾을 수 없습니다"));
        }

        String code = generateUniqueCode();

        // expiresAt 계산
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusHours(request.getExpirationHours() != null ? request.getExpirationHours() : 6);

        Invitation invitation = Invitation.builder()
                .code(code)
                .expiresAt(expiresAt)
                .maxUses(request.getMaxUses())
                .creator(creator)
                .meeting(meeting)  // meeting 객체를 직접 설정
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
                invitation.expire(); // 상태를 EXPIRED로 변경
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
        Meeting meeting = invitation.getMeeting();
        if (meeting == null) {
            throw new BaseException(MeetingResponseStatus.MEETING_NOT_FOUND);
        }

        // 이미 참여한 사용자인지 체크
        if (participantRepository.existsByUserAndMeeting(user, meeting)) {
            throw new BaseException(InvitationResponseStatus.INVITATION_ALREADY_JOINED);
        }

        // MeetingService를 통해 참가자 추가
        meetingService.joinMeeting(meeting.getMeetingId(), userId);

        // 미팅을 활성화 상태로 변경
        meeting.activateMeeting();
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