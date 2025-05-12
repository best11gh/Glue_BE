package org.glue.glue_be.post.service;


import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.meeting.entity.Meeting;
import org.glue.glue_be.meeting.entity.Participant;
import org.glue.glue_be.meeting.repository.MeetingRepository;
import org.glue.glue_be.meeting.repository.ParticipantRepository;
import org.glue.glue_be.meeting.response.MeetingResponseStatus;
import org.glue.glue_be.post.dto.request.CreatePostRequest;
import org.glue.glue_be.post.dto.response.CreatePostResponse;
import org.glue.glue_be.post.dto.response.GetPostResponse;
import org.glue.glue_be.post.entity.Like;
import org.glue.glue_be.post.entity.Post;
import org.glue.glue_be.post.repository.LikeRepository;
import org.glue.glue_be.post.repository.PostRepository;
import org.glue.glue_be.post.response.PostResponseStatus;
import org.glue.glue_be.user.entity.ProfileImage;
import org.glue.glue_be.user.entity.User;
import org.glue.glue_be.user.repository.UserRepository;
import org.glue.glue_be.user.response.UserResponseStatus;
import org.glue.glue_be.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Builder
@Slf4j
@Transactional
public class PostService {

	private final PostRepository postRepository;
	private final MeetingRepository meetingRepository;
	private final ParticipantRepository participantRepository;
	private final UserRepository userRepository;
	private final LikeRepository likeRepository;


	// 게시글 추가
	public CreatePostResponse createPost(CreatePostRequest request, String uuid) {

		CreatePostRequest.MeetingDto meetingRequest = request.getMeeting();
		CreatePostRequest.PostDto postRequest = request.getPost();

		// 1. validation
		if (meetingRequest.getMeetingTime().isBefore(LocalDateTime.now())) {
			throw new BaseException(MeetingResponseStatus.INVALID_MEETING_TIME);
		}

		// 2. 사용자 조회
		User creator = userRepository.findByUuid(UUID.fromString(uuid)).orElseThrow(() -> new BaseException(UserResponseStatus.USER_NOT_FOUND));

		// 3. 모임 생성
		Meeting meeting = Meeting.builder().meetingTitle(meetingRequest.getMeetingTitle()).meetingTime(meetingRequest.getMeetingTime()).meetingPlaceName(meetingRequest.getMeetingPlaceName())
			.minParticipants(0) // 1.0에선 최소인원이 안쓰이기에 일단 0으로 고정
			.maxParticipants(meetingRequest.getMaxParticipants()).currentParticipants(1).status(1).meetingPlaceLatitude(meetingRequest.getMeetingPlaceLatitude())
			.meetingPlaceLongitude(meetingRequest.getMeetingPlaceLongitude()).categoryId(meetingRequest.getCategoryId()).languageId(meetingRequest.getLanguageId()).host(creator) // 호스트 설정
			.build();
		Meeting savedMeeting = meetingRepository.save(meeting);

		// 4. 게시자를 참가자 테이블에 추가
		Participant participant = Participant.builder().user(creator).meeting(savedMeeting).build();
		participantRepository.save(participant);

		// 4.5. onetomany로 관리하는 participants 리스트에 추가
		// todo: 직접적인 연관관계 매핑이 너무 많아 생각보다 어려운듯 나중에 논의하기
		savedMeeting.addParticipant(participant);

		// 5. 게시글 생성
		Post post = Post.builder().meeting(savedMeeting).title(postRequest.getTitle()).content(postRequest.getContent()).build();
		Post savedPost = postRepository.save(post);

		// 6. responseDto 생성 직후 리턴
		return CreatePostResponse.builder().postId(savedPost.getId()).build();

	}


	// 게시글 단건 조회
	public GetPostResponse getPost(Long postId) {

		// 1. post, meeting 객체 가져오기
		Post post = postRepository.findById(postId).orElseThrow(() -> new BaseException(PostResponseStatus.POST_NOT_FOUND));
		Meeting meeting = post.getMeeting();

		// 2. 응답 dto 구성
		var participantDtos = meeting.getParticipants().stream().map(participant -> {
			User user = participant.getUser();
			String imageUrl = Optional.ofNullable(user.getProfileImage()).map(ProfileImage::getProfileImageUrl).orElse(null);
			return GetPostResponse.MeetingDto.ParticipantDto.builder().userId(user.getUserId()).nickname(user.getNickname()).profileImageUrl(imageUrl).build();
		}).collect(Collectors.toList());

		// 2.5. null일 확률이 있는 값은 Optional로 분기처리
		String creatorNickname = Optional.ofNullable(meeting.getHost()).map(User::getNickname).orElse("알 수 없는 사용자");
		String creatorImageUrl = Optional.ofNullable(meeting.getHost()).map(User::getProfileImage).map(ProfileImage::getProfileImageUrl).orElse(null);

		var meetingDto = GetPostResponse.MeetingDto.builder().meetingId(meeting.getMeetingId()).categoryId(meeting.getCategoryId()).creatorName(creatorNickname).creatorImageUrl(creatorImageUrl)
			.meetingTime(meeting.getMeetingTime()).currentParticipants(meeting.getCurrentParticipants()).maxParticipants(meeting.getMaxParticipants()).languageId(meeting.getLanguageId())
			.meetingStatus(meeting.getStatus()).participants(participantDtos).createdAt(meeting.getCreatedAt()).updatedAt(meeting.getUpdatedAt()).build();

		var postDto = GetPostResponse.PostDto.builder().postId(post.getId()).title(post.getTitle()).content(post.getContent()).viewCount(post.getViewCount()).bumpedAt(post.getBumpedAt())
			.likeCount(post.getLikes().size()).postImageUrl(post.getImages()).build();

		// 3. 조회했으므로 ++
		// todo: 일단은 조회마다 조회수 무조건 오르는 것으로 설정
		post.increaseViewCount();

		// 4. 조립이 완료된 응답 dto 리턴
		return GetPostResponse.builder().meeting(meetingDto).post(postDto).build();

	}


	// 게시글 끌올
	public void bumpPost(Long postId, UUID userUuid) {

		Post post = postRepository.findById(postId).orElseThrow(() -> new BaseException(PostResponseStatus.POST_NOT_FOUND));

		// 끌올은 게시글 작성자만 가능
		if (!post.getMeeting().isHost(userUuid)) throw new BaseException(PostResponseStatus.POST_NOT_AUTHOR);

		post.bump(LocalDateTime.now());
	}


	// 게시글 좋아요 토글
	public void toggleLike(Long postId, UUID userUuid) {

		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new BaseException(PostResponseStatus.POST_NOT_FOUND));

		User user = userRepository.findByUuid(userUuid)
			.orElseThrow(() -> new BaseException(UserResponseStatus.USER_NOT_FOUND));

		// 처음이면 좋아요 생성, 이미 좋아요 누른 상태면 좋아요 취소
		Optional<Like> existing = likeRepository.findByUserAndPost(user, post);

		if (existing.isPresent()) {
			likeRepository.delete(existing.get());
			post.getLikes().remove(existing.get()); //디비, 메모리는 별개 영역이라 직접 지우거나 orphanremoval 고려해야함..ㅠ
		} else {
			Like like = Like.builder()
				.user(user)
				.post(post)
				.build();
			likeRepository.save(like);
			post.getLikes().add(like);
		}
	}

}
