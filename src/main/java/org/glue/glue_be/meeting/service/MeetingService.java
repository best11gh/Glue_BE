package org.glue.glue_be.meeting.service;

import lombok.RequiredArgsConstructor;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.meeting.dto.MeetingDto;
import org.glue.glue_be.meeting.entity.*;
import org.glue.glue_be.meeting.repository.*;
import org.glue.glue_be.meeting.response.MeetingResponseStatus;
import org.glue.glue_be.notification.reminder.ReminderSchedulerService;
import org.glue.glue_be.post.entity.Post;
import org.glue.glue_be.post.repository.PostRepository;
import org.glue.glue_be.post.response.PostResponseStatus;
import org.glue.glue_be.user.entity.User;
import org.glue.glue_be.user.repository.UserRepository;
import org.glue.glue_be.user.response.UserResponseStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.glue.glue_be.user.entity.User.IS_DELETED;


@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final ParticipantRepository participantRepository;
    private final PostRepository postRepository;

    private final ReminderSchedulerService reminderSchedulerService;

    /**
     * 모임 생성
     */
    @Transactional
    public MeetingDto.CreateResponse createMeeting(MeetingDto.CreateRequest meetingRequest, Long creatorId) {

        // 사용자 조회
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new BaseException(UserResponseStatus.USER_NOT_FOUND));

        // 2. 모임 생성
        Meeting meeting = Meeting.builder()
            .meetingTitle(meetingRequest.getMeetingTitle())
            .meetingTime(meetingRequest.getMeetingTime())
            .meetingPlaceName(meetingRequest.getMeetingPlaceName())
            .minParticipants(0) // 1.0에선 최소인원이 안쓰이기에 일단 0으로 고정
            .maxParticipants(meetingRequest.getMaxParticipants())
            .currentParticipants(1)
            .status(1)
            .categoryId(meetingRequest.getCategoryId())
            .meetingMainLanguageId(meetingRequest.getMainLanguageId())
            .meetingExchangeLanguageId(meetingRequest.getExchangeLanguageId())
            .host(creator)
            .build();
        Meeting savedMeeting = meetingRepository.save(meeting);

        // 3. 게시자를 참가자 테이블에 추가
        Participant participant = Participant.builder().user(creator).meeting(savedMeeting).build();
        participantRepository.save(participant);

        // 3.5. onetomany로 관리하는 participants 리스트에 추가
        savedMeeting.addParticipant(participant);

        return MeetingDto.CreateResponse.builder()
                .meetingId(savedMeeting.getMeetingId())
                .build();
    }

    /**
     * 모임 참여
     */
    @Transactional
    public void joinMeeting(Long meetingId, Long userId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BaseException(MeetingResponseStatus.MEETING_NOT_FOUND));

        if (meeting.getHost().getIsDeleted().equals(IS_DELETED)) {
            throw new BaseException(MeetingResponseStatus.MEETING_HOST_DELETED);
        }

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

        // 리마인더 등록
        Post post = postRepository.findByMeetingId(meetingId)
                .orElseThrow(() -> new BaseException(PostResponseStatus.POST_NOT_FOUND));

        reminderSchedulerService.scheduleReminder(userId, post.getId(), meeting.getMeetingTime());
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
