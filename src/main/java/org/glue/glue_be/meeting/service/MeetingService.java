package org.glue.glue_be.meeting.service;

import lombok.RequiredArgsConstructor;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.common.response.BaseResponseStatus;
import org.glue.glue_be.invitation.dto.InvitationDto;
import org.glue.glue_be.invitation.response.InvitationResponseStatus;
import org.glue.glue_be.invitation.service.InvitationService;
import org.glue.glue_be.meeting.dto.MeetingDto;
import org.glue.glue_be.meeting.entity.Meeting;
import org.glue.glue_be.meeting.entity.Participant;
import org.glue.glue_be.meeting.repository.MeetingRepository;
import org.glue.glue_be.meeting.repository.ParticipantRepository;
import org.glue.glue_be.meeting.response.MeetingResponseStatus;
import org.glue.glue_be.user.entity.User;
import org.glue.glue_be.user.repository.UserRepository;
import org.glue.glue_be.user.response.UserResponseStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final ParticipantRepository participantRepository;
    private final InvitationService invitationService;

    /**
     * 모임 생성
     */
    @Transactional
    public MeetingDto.CreateResponse createMeeting(MeetingDto.CreateRequest request, Long creatorId) {
        // 유효성 검증
        if (request.getMinPpl() > request.getMaxPpl()) {
            throw new BaseException(MeetingResponseStatus.MIN_OVER_MAX);
        }

        if (request.getMeetingTime().isBefore(LocalDateTime.now())) {
            throw new BaseException(MeetingResponseStatus.INVALID_MEETING_TIME);
        }

        // 사용자 조회
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new BaseException(UserResponseStatus.USER_NOT_FOUND));

        // 모임 생성
        Meeting meeting = Meeting.builder()
                .meetingTitle(request.getMeetingTitle())
                .meetingTime(request.getMeetingTime())
                .meetingPlaceName(request.getMeetingPlaceName())
                .meetingPlaceLatitude(request.getMeetingPlaceLatitude())
                .meetingPlaceLongitude(request.getMeetingPlaceLongitude())
                .minParticipants(request.getMinPpl())
                .maxParticipants(request.getMaxPpl())
                .currentParticipants(1) // 생성자가 첫 번째 참가자
                .status(1) // 1: 활성화 상태
                .host(creator) // 호스트 설정
                .build();

        Meeting savedMeeting = meetingRepository.save(meeting);

        // 생성자를 참가자로 추가
        Participant participant = Participant.builder()
                .user(creator)
                .meeting(savedMeeting)
                .build();
        
        participantRepository.save(participant);
        savedMeeting.addParticipant(participant);

        return MeetingDto.CreateResponse.builder()
                .meetingId(savedMeeting.getMeetingId())
                .build();
    }

    /**
     * 모임 초대장 생성
     */
    @Transactional
    public InvitationDto.Response createMeetingInvitation(Long meetingId, Long inviteeId, Long creatorId) {
        // 모임 존재 확인
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BaseException(MeetingResponseStatus.MEETING_NOT_FOUND));

        // 초대할 사용자 존재 확인
        User invitee = userRepository.findById(inviteeId)
                .orElseThrow(() -> new BaseException(UserResponseStatus.USER_NOT_FOUND));

        // 초대 생성자가 모임의 호스트인지 확인
        if (!meeting.isHost(creatorId)) {
            throw new BaseException(MeetingResponseStatus.NOT_HOST_PERMISSION);
        }

        // 이미 참가자인지 확인
        if (participantRepository.existsByUserAndMeeting(invitee, meeting)) {
            throw new BaseException(InvitationResponseStatus.INVITATION_ALREADY_JOINED);
        }

        // 초대장 생성
        InvitationDto.CreateRequest invitationRequest = new InvitationDto.CreateRequest();
        invitationRequest.setMeetingId(meetingId);
        invitationRequest.setMaxUses(1); // 1회용 초대장
        invitationRequest.setExpirationDays(0); // 6시간 후 만료 (0.25일)
        invitationRequest.setExpirationHours(6); // 6시간
        invitationRequest.setInviteeId(inviteeId); // 초대된 사용자 ID 설정

        return invitationService.createInvitation(invitationRequest, creatorId);
    }

    /**
     * 모임 참여
     */
    @Transactional
    public void joinMeeting(Long meetingId, Long userId) {
        // 모임 존재 확인
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BaseException(MeetingResponseStatus.MEETING_NOT_FOUND));

        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(UserResponseStatus.USER_NOT_FOUND));

        // 이미 참가자인지 확인
        if (participantRepository.existsByUserAndMeeting(user, meeting)) {
            throw new BaseException(MeetingResponseStatus.ALREADY_JOINED);
        }

        // 모임이 꽉 찼는지 확인
        if (meeting.isMeetingFull()) {
            throw new BaseException(MeetingResponseStatus.MEETING_FULL);
        }

        // 참가자 추가
        Participant participant = Participant.builder()
                .user(user)
                .meeting(meeting)
                .build();

        participantRepository.save(participant);
        meeting.addParticipant(participant);
    }

    /**
     * 모임 조회
     */
    @Transactional(readOnly = true)
    public MeetingDto.Response getMeeting(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BaseException(MeetingResponseStatus.MEETING_NOT_FOUND));
        
        return MeetingDto.Response.from(meeting);
    }
}
