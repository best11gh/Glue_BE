package org.glue.glue_be.post.service;


import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.meeting.entity.Meeting;
import org.glue.glue_be.meeting.entity.Participant;
import org.glue.glue_be.meeting.repository.MeetingRepository;
import org.glue.glue_be.meeting.repository.ParticipantRepository;
import org.glue.glue_be.meeting.response.MeetingResponseStatus;
import org.glue.glue_be.post.dto.request.CreatePostRequest;
import org.glue.glue_be.post.dto.response.CreatePostResponse;
import org.glue.glue_be.post.entity.Post;
import org.glue.glue_be.post.repository.PostRepository;
import org.glue.glue_be.user.entity.User;
import org.glue.glue_be.user.repository.UserRepository;
import org.glue.glue_be.user.response.UserResponseStatus;
import org.glue.glue_be.user.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Builder
public class PostService {

	private final PostRepository postRepository;
	private final MeetingRepository meetingRepository;
	private final ParticipantRepository participantRepository;
	private final UserRepository userRepository;

	// 게시글 추가
	public CreatePostResponse createPost(CreatePostRequest request, String uuid) {

		CreatePostRequest.MeetingDto meetingRequest = request.getMeeting();
		CreatePostRequest.PostDto postRequest = request.getPost();

		// 1. validation
		if (meetingRequest.getMeetingTime().isBefore(LocalDateTime.now())) {
			throw new BaseException(MeetingResponseStatus.INVALID_MEETING_TIME);
		}

		// 2. 사용자 조회
		User creator = userRepository.findByUuid(UUID.fromString(uuid))
			.orElseThrow(() -> new BaseException(UserResponseStatus.USER_NOT_FOUND));


		// 3. 모임 생성
		Meeting meeting = Meeting.builder()
			.meetingTitle(meetingRequest.getMeetingTitle())
			.meetingTime(meetingRequest.getMeetingTime())
			.meetingPlaceName(meetingRequest.getMeetingPlaceName())
			.minParticipants(0) // 1.0에선 최소인원이 안쓰이기에 일단 0으로 고정
			.maxParticipants(meetingRequest.getMaxParticipants())
			.currentParticipants(1)
			.status(1)
			.meetingPlaceLatitude(meetingRequest.getMeetingPlaceLatitude())
			.meetingPlaceLongitude(meetingRequest.getMeetingPlaceLongitude())
			.categoryId(meetingRequest.getCategoryId())
			.languageId(meetingRequest.getLanguageId())
			.host(creator) // 호스트 설정
			.build();
		Meeting savedMeeting = meetingRepository.save(meeting);

		// 4. 게시자를 참가자 테이블에 추가
		Participant participant = Participant.builder()
			.user(creator)
			.meeting(savedMeeting)
			.build();
		participantRepository.save(participant);

		// 4.5. onetomany로 관리하는 participants 리스트에 추가
		// todo: 직접적인 연관관계 매핑 세팅이 너무 많아 생각보다 어려운듯 나중에 논의하기
		savedMeeting.addParticipant(participant);

		// 5. 게시글 생성
		Post post = Post.builder()
			.meeting(savedMeeting)
			.title(postRequest.getTitle())
			.content(postRequest.getContent())
			.build();
		Post savedPost = postRepository.save(post);

		// 6. responseDto 생성 직후 리턴
		return CreatePostResponse.builder()
			.postId(savedPost.getId())
			.build();

	}

}
