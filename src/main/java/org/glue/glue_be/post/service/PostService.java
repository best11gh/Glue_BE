package org.glue.glue_be.post.service;


import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.common.config.LocalDateTimeStringConverter;
import org.glue.glue_be.common.dto.UserSummary;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.meeting.entity.*;
import org.glue.glue_be.meeting.repository.*;
import org.glue.glue_be.meeting.response.MeetingResponseStatus;
import org.glue.glue_be.notification.reminder.ReminderSchedulerService;
import org.glue.glue_be.post.dto.request.CreatePostRequest;
import org.glue.glue_be.post.dto.response.*;
import org.glue.glue_be.post.entity.*;
import org.glue.glue_be.post.repository.*;
import org.glue.glue_be.post.response.PostResponseStatus;
import org.glue.glue_be.user.entity.User;
import org.glue.glue_be.user.repository.UserRepository;
import org.glue.glue_be.user.response.UserResponseStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
	private final PostImageRepository postImageRepository;

	private final ReminderSchedulerService reminderSchedulerService;

	// 게시글 사진이 추가 및 삭제될 때 meeting image url도 함께 업데이트 시키는 메소드
	public void updateMeetingImageUrl(Long meetingId) {
		meetingRepository.updateMeetingImageUrl(meetingId);
	}

	// 게시글 추가
	public CreatePostResponse createPost(CreatePostRequest request, Long userId) {

		CreatePostRequest.MeetingDto meetingRequest = request.getMeeting();
		CreatePostRequest.PostDto postRequest = request.getPost();

		// 1. 사용자 조회
		User creator = userRepository.findById(userId).orElseThrow(() -> new BaseException(UserResponseStatus.USER_NOT_FOUND));

		// 2. 모임 생성
		Meeting meeting = Meeting.builder() // todo: toEntity 팩토리 메서드로 대체
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
			.host(creator)
			.build();
		Meeting savedMeeting = meetingRepository.save(meeting);

		// 3. 게시자를 참가자 테이블에 추가
		Participant participant = Participant.builder().user(creator).meeting(savedMeeting).build();
		participantRepository.save(participant);

		// 3.5. onetomany로 관리하는 participants 리스트에 추가
		// todo: 직접적인 연관관계 매핑이 너무 많아 생각보다 어려운듯 나중에 논의하기
		savedMeeting.addParticipant(participant);

		// 4. 게시글 생성
		Post post = Post.builder().meeting(savedMeeting).title(postRequest.getTitle()).content(postRequest.getContent()).build();
		Post savedPost = postRepository.save(post);

		// 4.5. 게시글 생성 시 이미지주소 있다면 DB에 저장 후 양방향 매핑 작업도 수행
		List<String> imageUrls = postRequest.getImageUrls();

		if(imageUrls != null && !imageUrls.isEmpty()) {
			int order = 0;
			for (String imageUrl : imageUrls) {
				PostImage postImage = PostImage.builder()
					.post(savedPost)
					.imageUrl(imageUrl)
					.imageOrder(order++)
					.build();

				postImageRepository.save(postImage);
				savedPost.getImages().add(postImage); // 양방향 추가 처리
			}
		}

		// 5. 이미지가 저장된 후 미팅 대표 이미지 업데이트
		updateMeetingImageUrl(savedMeeting.getMeetingId());

		// 5.5 알림 예약 - 모임 생성자한테 가는 용도
		reminderSchedulerService.scheduleReminder(userId, savedPost.getId(), savedMeeting.getMeetingTime());

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

			return GetPostResponse.MeetingDto.ParticipantDto.builder()
				.userId(user.getUserId())
				.nickname(user.getNickname())
				.profileImageUrl(user.getProfileImageUrl()).build();
		}).collect(Collectors.toList());

		// creator 정보는 common dto인 UserSummary 사용
		UserSummary creator = Optional.ofNullable(meeting.getHost())
			.map(host -> UserSummary.builder()
				.userId(host.getUserId())
				.userNickname(host.getNickname())
				.profileImageUrl(host.getProfileImageUrl())
				.build())
			.orElse(null);

		List<GetPostResponse.PostDto.PostImageDto> imageUrls = post.getImages().stream()
			.map(pi -> GetPostResponse.PostDto.PostImageDto.builder()
				.postImageId(pi.getId())
				.imageUrl(pi.getImageUrl())
				.imageOrder(pi.getImageOrder())
				.build())
			.toList(); // 게시글 이미지

		var meetingDto = GetPostResponse.MeetingDto.builder().
			meetingId(meeting.getMeetingId())
			.categoryId(meeting.getCategoryId())
			.creator(creator)
			.meetingTime(meeting.getMeetingTime())
			.currentParticipants(meeting.getCurrentParticipants())
			.maxParticipants(meeting.getMaxParticipants())
			.languageId(meeting.getLanguageId())
			.meetingStatus(meeting.getStatus())
			.participants(participantDtos)
			.createdAt(meeting.getCreatedAt())
			.updatedAt(meeting.getUpdatedAt())
			.build();

		var postDto = GetPostResponse.PostDto.builder()
			.postId(post.getId())
			.title(post.getTitle())
			.content(post.getContent())
			.viewCount(post.getViewCount())
			.bumpedAt(post.getBumpedAt())
			.likeCount(post.getLikes().size())
			.postImageUrl(imageUrls)
			.build();

		// 3. dto 조립
		GetPostResponse response = GetPostResponse.builder()
			.meeting(meetingDto)
			.post(postDto)
			.build();

		// 4. 조회수 증가 후 리턴
		post.increaseViewCount();

		return response;

	}


	// 게시글 끌올
	public void bumpPost(Long postId, Long userId) {

		Post post = postRepository.findById(postId).orElseThrow(() -> new BaseException(PostResponseStatus.POST_NOT_FOUND));

		// 끌올은 게시글 작성자만 가능
		if (!post.getMeeting().isHost(userId)) throw new BaseException(PostResponseStatus.POST_NOT_AUTHOR);

		post.bump(LocalDateTime.now());
	}


	// 게시글 좋아요 토글
	public void toggleLike(Long postId, Long userId) {

		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new BaseException(PostResponseStatus.POST_NOT_FOUND));

		User user = userRepository.findById(userId)
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

	@Transactional(readOnly = true)
	public GetPostsResponse getPosts(Long lastPostId, int size, Integer categoryId) {

		int limit = size + 1;
		List<Post> result;

		if (categoryId == null) { // 카테고리 지정이 아닌 경우

			if (lastPostId == null) { // 최초 페이지
				result = postRepository.fetchFirstPage(limit);
			} else { // 2번째 이상 페이지 -> 이전 페이지의 마지막 게시글을 커서로 삼아 다음 스크롤 fetch
				Post cursor = postRepository.findById(lastPostId)
						.orElseThrow(() -> new BaseException(PostResponseStatus.POST_NOT_FOUND));

				LocalDateTime cursorSortAt = cursor.getBumpedAt() != null
						? cursor.getBumpedAt()
						: cursor.getMeeting().getCreatedAt();

				String cursorSortAtString = new LocalDateTimeStringConverter().convertToDatabaseColumn(cursorSortAt);

				result = postRepository.fetchNextPage(cursorSortAtString, lastPostId, limit);
			}
		} else { // 카테고리 지정!!

			if (lastPostId == null) {
				result = postRepository.fetchFirstPageByCategory(categoryId, limit);
			} else {
				Post cursor = postRepository.findById(lastPostId)
						.orElseThrow(() -> new BaseException(PostResponseStatus.POST_NOT_FOUND));

				LocalDateTime cursorSortAt = cursor.getBumpedAt() != null
						? cursor.getBumpedAt()
						: cursor.getMeeting().getCreatedAt();

				String cursorSortAtString = new LocalDateTimeStringConverter().convertToDatabaseColumn(cursorSortAt);

				result = postRepository.fetchNextPageByCategory(categoryId, cursorSortAtString, lastPostId, limit);
			}
		}

		boolean hasNext = result.size() > size;
		// 필요로 하는 size보다 1개를 더 가져와보는데 잘 받아와진다? == 다음 스크롤을 위한 게시글이 적어도 1개 존재 == hasNext is True

		if (hasNext) result = result.subList(0, size);

		return GetPostsResponse.builder()
				.hasNext(hasNext)
				.posts(result.stream()
						.map(GetPostsResponse::ofEntity)
						.collect(Collectors.toList()))
				.build();

	}


	// TODO: 게시글 수정 시 해야할 것 들
	// 1. reminderSchedulerService의 rescheduleReminder 함수를 써야함. (리마인더 새로 등록)
	// 2. 모임에 모인 사람들한테 게시글 수정 알림을 보내주어야 함. - NotificationService의 createBulk함수 사용 (모임을 만든 사람 제외!!!)

	// TODO: 게시글 삭제 시 해야할 것 - reminderSchedulerService의 removeRemindersByPost 함수 사용


}
