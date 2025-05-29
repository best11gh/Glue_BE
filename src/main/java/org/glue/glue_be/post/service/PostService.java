package org.glue.glue_be.post.service;


import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.aws.service.FileService;
import org.glue.glue_be.chat.entity.dm.DmChatRoom;
import org.glue.glue_be.chat.entity.group.GroupChatRoom;
import org.glue.glue_be.chat.repository.dm.DmChatRoomRepository;
import org.glue.glue_be.chat.repository.dm.DmMessageRepository;
import org.glue.glue_be.chat.repository.dm.DmUserChatroomRepository;
import org.glue.glue_be.chat.repository.group.GroupChatRoomRepository;
import org.glue.glue_be.chat.repository.group.GroupMessageRepository;
import org.glue.glue_be.chat.repository.group.GroupUserChatRoomRepository;
import org.glue.glue_be.common.config.LocalDateTimeStringConverter;
import org.glue.glue_be.common.dto.UserSummary;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.invitation.repository.InvitationRepository;
import org.glue.glue_be.meeting.entity.*;
import org.glue.glue_be.meeting.repository.*;
import org.glue.glue_be.notification.dto.request.BulkNotificationRequest;
import org.glue.glue_be.notification.reminder.ReminderSchedulerService;
import org.glue.glue_be.notification.service.NotificationService;
import org.glue.glue_be.post.dto.request.CreatePostRequest;
import org.glue.glue_be.post.dto.request.UpdatePostRequest;
import org.glue.glue_be.post.dto.response.*;
import org.glue.glue_be.post.entity.*;
import org.glue.glue_be.post.repository.*;
import org.glue.glue_be.post.response.PostImageResponseStatus;
import org.glue.glue_be.post.response.PostResponseStatus;
import org.glue.glue_be.user.entity.User;
import org.glue.glue_be.user.repository.UserRepository;
import org.glue.glue_be.user.response.UserResponseStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Builder
@Slf4j
@Transactional
public class PostService {

	// 게시글 관련
	private final PostRepository postRepository;
	private final MeetingRepository meetingRepository;
	private final ParticipantRepository participantRepository;
	private final UserRepository userRepository;
	private final LikeRepository likeRepository;
	private final PostImageRepository postImageRepository;
	private final InvitationRepository invitationRepository;
	// 채팅 관련
	private final DmChatRoomRepository dmChatRoomRepository;
	private final DmUserChatroomRepository dmUserChatroomRepository;
	private final DmMessageRepository dmMessageRepository;
	private final GroupChatRoomRepository groupChatRoomRepository;
	private final GroupUserChatRoomRepository groupUserChatRoomRepository;
	private final GroupMessageRepository groupMessageRepository;

	private final ReminderSchedulerService reminderSchedulerService;
	private final FileService fileService;
	private final NotificationService notificationService;


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
	public GetPostResponse getPost(Long postId, Long userId) {

		// 1. post, meeting, user 객체 가져오기
		Post post = postRepository.findById(postId).orElseThrow(() -> new BaseException(PostResponseStatus.POST_NOT_FOUND));
		Meeting meeting = post.getMeeting();


		// 2. 응답 dto 구성
		var participantDtos = meeting.getParticipants().stream().map(participant -> {
			User participantUser = participant.getUser();

			return GetPostResponse.MeetingDto.ParticipantDto.builder()
				.userId(participantUser.getUserId())
				.nickname(participantUser.getNickname())
				.profileImageUrl(participantUser.getProfileImageUrl()).build();
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

		Boolean isLiked = likeRepository.existsByUser_UserIdAndPost_Id(userId, postId);

		var meetingDto = GetPostResponse.MeetingDto.builder()
			.meetingId(meeting.getMeetingId())
			.categoryId(meeting.getCategoryId())
			.meetingTime(meeting.getMeetingTime())
			.currentParticipants(meeting.getCurrentParticipants())
			.meetingPlaceName(meeting.getMeetingPlaceName())
			.maxParticipants(meeting.getMaxParticipants())
			.mainLanguageId(meeting.getMeetingMainLanguageId())
			.exchangeLanguageId(meeting.getMeetingExchangeLanguageId())
			.meetingStatus(meeting.getStatus())
			.createdAt(meeting.getCreatedAt())
			.updatedAt(meeting.getUpdatedAt())
			.participants(participantDtos)
			.creator(creator)
			.build();

		var postDto = GetPostResponse.PostDto.builder()
			.postId(post.getId())
			.title(post.getTitle())
			.content(post.getContent())
			.viewCount(post.getViewCount())
			.bumpedAt(post.getBumpedAt())
			.bumpedCount(post.getBumpCount())
			.bumpLimit(Post.BUMP_LIMIT)
			.likeCount(post.getLikes().size())
			.isLiked(isLiked)
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
	public BumpPostResponse bumpPost(Long postId, Long userId) {
		Post post = postRepository.findById(postId).orElseThrow(() -> new BaseException(PostResponseStatus.POST_NOT_FOUND));

		// 끌올은 게시글 작성자만 가능
		if (!post.getMeeting().isHost(userId)) throw new BaseException(PostResponseStatus.POST_NOT_AUTHOR);

		post.bump(LocalDateTime.now());

		return new BumpPostResponse(
			post.getBumpCount(),
			Post.BUMP_LIMIT
		);

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

	// 게시글 목록 조회
	// 무한스크롤 + 카테고리별 필터링
	@Transactional(readOnly = true)
	public GetPostsResponse getPosts(Long lastPostId, int size, Integer categoryId, Long userId) {

		int limit = size + 1;
		List<Post> result;

		if (categoryId == null) { // 카테고리 지정이 아닌 경우

			if (lastPostId == null) { // 최초 페이지
				result = postRepository.fetchFirstPage(limit);
			} else { // 2번째 이상 페이지 -> 이전 페이지의 마지막 게시글을 커서로 삼아 다음 스크롤 fetch
				Post cursor = postRepository.findById(lastPostId)
						.orElseThrow(() -> new BaseException(PostResponseStatus.POST_NOT_FOUND));

				LocalDateTime cursorTimeStamp = cursor.getBumpedAt() != null
						? cursor.getBumpedAt()
						: cursor.getMeeting().getCreatedAt();

				String cursorTimeStampString = new LocalDateTimeStringConverter().convertToDatabaseColumn(cursorTimeStamp);

				result = postRepository.fetchNextPage(cursorTimeStampString, lastPostId, limit);
			}
		} else { // 카테고리 지정!!

			if (lastPostId == null) {
				result = postRepository.fetchFirstPageByCategory(categoryId, limit);
			} else {
				Post cursor = postRepository.findById(lastPostId)
						.orElseThrow(() -> new BaseException(PostResponseStatus.POST_NOT_FOUND));

				LocalDateTime cursorTimeStamp = cursor.getBumpedAt() != null
						? cursor.getBumpedAt()
						: cursor.getMeeting().getCreatedAt();

				String cursorTimeStampString = new LocalDateTimeStringConverter().convertToDatabaseColumn(cursorTimeStamp);

				result = postRepository.fetchNextPageByCategory(categoryId, cursorTimeStampString, lastPostId, limit);
			}
		}

		boolean hasNext = result.size() > size;
		// 필요로 하는 size보다 1개를 더 가져와보는데 잘 받아와진다? == 다음 스크롤을 위한 게시글이 적어도 1개 존재 == hasNext is True

		if (hasNext) result = result.subList(0, size);

		// [유저의 각 게시글 좋아요 여부를 확인하는 절차]
		// 1. 가져온 게시글들의 id만의 목록을 생성
		List<Long> postIds = result.stream().map(Post::getId).toList();

		// 2. Like 테이블의 user_id가 userID와 같고 동시에 post_id가 postIds에 들어있는 것들의 post_id 리턴 (by sql IN 문법)
		List<Long> likedPostIds = likeRepository.findLikedPostIdsByUserAndPostIds(userId, postIds);
		Set<Long> likedPostIdsSet = new HashSet<>(likedPostIds); // hash 조회 = O(1)

		return GetPostsResponse.builder()
				.hasNext(hasNext)
				.posts(result.stream()
						.map(post -> GetPostsResponse.ofEntity(post, likedPostIdsSet.contains(post.getId())))
						.collect(Collectors.toList()))
				.build();

	}

	// 게시글 삭제 -> cascade 쓰면 딸깍이긴 할텐데 주의해야하는 부분이긴하다..ㅠㅠㅠㅠ
	// 삭제할 엔티티들간의 연관관계 도식도
	//	POST ──┐─ PostImage, Like
	//	       │
	//         │
	//         │
	//	    MEETING ────┬─── Participant, Invitation
	//                  ├─── DmChatRoom─────┬─ DmUserChatroom
	//                  │                   └─ DmMessage
	//                  └─── GroupChatRoom ─┬─ GroupUserChatRoom
	//                                      └─ GroupMessage
	public void deletePost(Long postId, Long userId) {

		// 1. 삭제할 post, meeting을 가져온다. 이 둘을 루트로 하는 연관관계 엔티티 요소들을 싹 삭제
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new BaseException(PostResponseStatus.POST_NOT_FOUND));
		Meeting meeting = post.getMeeting();
		Long meetingId = meeting.getMeetingId();

		// 2. 게시자만 삭제가능
		if(!meeting.isHost(userId)) throw new BaseException(PostResponseStatus.POST_NOT_AUTHOR);

		// 2.5. 리마인더 제거호출 위한 참여자 id 목록 제작
		List<Long> participantsIdList = meeting.getParticipants().stream().map(
			participant -> participant.getUser().getUserId()
		).toList();

		// 3. 리마인더 제거
		reminderSchedulerService.removeRemindersByPost(postId, participantsIdList);

		// 4. Post 연관 엔티티 삭제
		List<PostImage> postImages = postImageRepository.findAllByPost_Id(postId);

		// 4-1. s3 버킷에 삭제요청
		for (PostImage postImage : postImages) {
			String url = postImage.getImageUrl();
			try {
				fileService.deleteFile(url);
			} catch (Exception e) {
				log.error("해당 s3 이미지 url을 삭제하는데 실패! -> {}, db 삭제는 지속됨", url, e);
				// (언젠가 투두) 실패 항목을 DB나 메시지 큐에 기록 → 백그라운드 재시도
				// failedDeletionQueue.add(url);
			}
		}

		// 4-2. PostImage, Like 삭제
		postImageRepository.deleteByPost_Id(postId);
		likeRepository.deleteByPost_Id(postId);

		// 4-3. Post 삭제
		postRepository.delete(post);


		// 5. Meeting 연관 엔티티 삭제
		// 5-1. Participants, Invitation 삭제
		participantRepository.deleteByMeeting_MeetingId(meetingId);
		invitationRepository.deleteByMeeting_MeetingId(meetingId);

		// 5-2. DM챗 삭제작업
		List<DmChatRoom> dmChatRooms = dmChatRoomRepository.findByMeeting_MeetingId(meetingId);
		for(DmChatRoom dmChatRoom : dmChatRooms){
			Long roomId = dmChatRoom.getId();
			dmUserChatroomRepository.deleteByDmChatRoom_Id(roomId);
			dmChatRoomRepository.delete(dmChatRoom);
		}
		// 남은 dmChatRoom 벌크를 N번이 아닌 1번에 삭제 (자식 엔티티 다 삭제했으니 벌크로 1번에 몰살해도 안전함이 보장된 상황)
		dmChatRoomRepository.deleteAllInBatch(dmChatRooms);

		List<GroupChatRoom> groupRooms = groupChatRoomRepository.findByMeeting_MeetingId(meetingId);
		for (GroupChatRoom groupRoom : groupRooms) {
			Long roomId = groupRoom.getGroupChatroomId();
			groupUserChatRoomRepository.deleteByGroupChatroom_GroupChatroomId(roomId);
			groupMessageRepository.deleteByGroupChatroom_GroupChatroomId(roomId);
		}
		groupChatRoomRepository.deleteAllInBatch(groupRooms);

		// 5-3. meeting 최종 삭제 후 로그출력
		meetingRepository.delete(meeting);

		log.info("Post {}와 Meeting {}이 User {}에 의해 삭제완료됐습니다", postId, meetingId, userId);

	}


	// 게시글 검색
	@Transactional(readOnly = true)
	public GetPostsResponse searchPosts(Long lastPostId, int size, String keyword, Long userId) {

		int limit = size + 1;
		String kw = "%" + keyword + "%";
		List<Post> result;

		if (lastPostId == null) {
			// 최초 검색 페이지
			result = postRepository.fetchFirstPageByKeyword(kw, limit);
		} else {
			// 커서로 다음 검색 페이지
			Post cursor = postRepository.findById(lastPostId)
				.orElseThrow(() -> new BaseException(PostResponseStatus.POST_NOT_FOUND));

			LocalDateTime cursorTimeStamp = cursor.getBumpedAt() != null
				? cursor.getBumpedAt()
				: cursor.getMeeting().getCreatedAt();

			String cursorTimeStampString =
				new LocalDateTimeStringConverter().convertToDatabaseColumn(cursorTimeStamp);

			result = postRepository.fetchNextPageByKeyword(
				kw, cursorTimeStampString, lastPostId, limit
			);
		}

		boolean hasNext = result.size() > size;
		if (hasNext) {
			result = result.subList(0, size);
		}

		// [좋아요 여부까지 포함]
		List<Long> postIds = result.stream().map(Post::getId).toList();
		Set<Long> likedPostIds = new HashSet<>(
			likeRepository.findLikedPostIdsByUserAndPostIds(userId, postIds)
		);

		return GetPostsResponse.builder()
			.hasNext(hasNext)
			.posts(
				result.stream()
					.map(post -> GetPostsResponse.ofEntity(post,
						likedPostIds.contains(post.getId())))
					.collect(Collectors.toList())
			)
			.build();
	}


	public void updatePost(Long postId, Long userId, UpdatePostRequest req) {
		Post post = postRepository.findById(postId).orElseThrow(() -> new BaseException(PostResponseStatus.POST_NOT_FOUND));
		Meeting meeting = post.getMeeting();

		// 1. 글쓴 유저인지 + 지금이 기존 모임 시각 3시간 이내인지 검증
		LocalDateTime now = LocalDateTime.now();
		if(!meeting.isHost(userId)) throw new BaseException(PostResponseStatus.POST_NOT_AUTHOR);
		if(meeting.getMeetingTime().isBefore(now.plusHours(Meeting.UPDATE_LIMIT_HOUR))) throw new BaseException(PostResponseStatus.POST_CANNOT_UPDATE_CLOSE_TO_MEETING);

		// 2. meeting 칼럼 수정
		UpdatePostRequest.MeetingDto m = req.getMeeting();
		meeting.updateMeeting(
			m.getMeetingTitle(), m.getMeetingPlaceName(), m.getMeetingTime(),
			m.getMainLanguageId(), m.getExchangeLanguageId(), m.getMaxParticipants()
		);

		// 3. post 칼럼 수정
		UpdatePostRequest.PostDto p = req.getPost();
		post.updatePost(p.getTitle(), p.getContent());


		// 4. 이미지 처리
		List<String> newUrls = p.getImageUrls() != null ? p.getImageUrls() : List.of();

		// 4-1) 기존 imageUrl과 수정 imageUrl을 set에 넣음
		List<PostImage> existingUrls = post.getImages();
		Set<String> existingSet = existingUrls.stream().map(PostImage::getImageUrl).collect(Collectors.toSet());
		Set<String> newSet = new HashSet<>(newUrls);

		// 4-2) 삭제해야할 urls 수집 및 삭제: existingSet 중 newSet에 없는 것들만 추려내서 s3 버킷, PostImage 레코드 삭제
		List<String> toDelete = existingSet.stream().filter(url -> !newSet.contains(url)).toList();
		for (String url : toDelete) {
			try {
				fileService.deleteFile(url);
			} catch (Exception e) {
				log.error("해당 s3 이미지 url을 삭제하는데 실패! -> {}, db 삭제는 지속됨: {}", url, e.getMessage());
			}
			postImageRepository.deleteByPost_IdAndImageUrl(postId, url);
		}

		// 4-3) 입력으로 들어온 새로운 imageUrls 처리
		int order = 0;
		for (String url : newUrls) {
			if (existingSet.contains(url)) {
				// 기존에 있다면 순서만 업데이트
				PostImage pi = existingUrls.stream()
					.filter(ei -> ei.getImageUrl().equals(url))
					.findFirst()
					.orElseThrow(() -> new BaseException(PostImageResponseStatus.POST_IMAGE_NOT_FOUND));
				pi.updateImageOrder(order);
			} else {
				// 신규는 엔티티 생성
				PostImage pi = PostImage.builder()
					.post(post)
					.imageUrl(url)
					.imageOrder(order)
					.build();
				postImageRepository.save(pi);
				post.getImages().add(pi);
			}
			order++;
		}

		// 5. 미팅 썸네일 수정
		updateMeetingImageUrl(meeting.getMeetingId());

		// 6. 리마인더 새로 등록
		reminderSchedulerService.rescheduleReminder(userId, postId, meeting.getMeetingTime());

		// 7. host 제외 모임 수정 알림 발송
		List<Long> receiverIds = meeting.getParticipants().stream()
			.map(pt -> pt.getUser().getUserId())
			.filter(id -> !id.equals(meeting.getHost().getUserId()))
			.toList();

		BulkNotificationRequest bulkReq = BulkNotificationRequest.builder()
			.receiverIds(receiverIds)
			.type("post")
			.title("모임글에 수정이 발생했습니다. 확인해주세요!")
			.content(p.getTitle() + " 게시글이 수정되었습니다.")
			.targetId(postId)
			.guestbookHostId(null)
			.build();

		notificationService.createBulk(bulkReq);
	}


	// PostService.java
	@Transactional(readOnly = true)
	public List<MainPagePostResponse> getMainPagePosts(int size, Long userId) {
		LocalDateTime now = LocalDateTime.now();
		Pageable page = PageRequest.of(0, size);

		// 1) 미팅 시간이 지나지 않은 인기 게시글 size개 조회
		List<Post> posts = postRepository.findPopularPosts(now, page);

		// 2) 로그인한 사용자의 좋아요 상태
		List<Long> postIds = posts.stream().map(Post::getId).toList();
		Set<Long> likedSet = new HashSet<>(
			likeRepository.findLikedPostIdsByUserAndPostIds(userId, postIds)
		);

		// 3) MainPagePostResponse 로 매핑
		return posts.stream()
			.map(p -> MainPagePostResponse.builder()
				.postId(Math.toIntExact(p.getId()))
				.categoryId(p.getMeeting().getCategoryId())
				.createdAt(p.getMeeting().getCreatedAt())
				.title(p.getTitle())
				.content(p.getContent())
				.likeCount(p.getLikes().size())
				.isLiked(likedSet.contains(p.getId()) ? 1 : 0)
				.currentParticipants(p.getMeeting().getCurrentParticipants())
				.maxParticipants(p.getMeeting().getMaxParticipants())
				.build()
			).toList();
	}

	@Transactional(readOnly = true)
	public GetPostsResponse getPopularDetailed(int size, Long userId) {
		LocalDateTime now = LocalDateTime.now();
		Pageable page = PageRequest.of(0, size);
		List<Post> posts = postRepository.findPopularPosts(now, page);

		List<Long> ids = posts.stream().map(Post::getId).toList();
		Set<Long> liked = new HashSet<>( likeRepository.findLikedPostIdsByUserAndPostIds(userId, ids) );

		List<GetPostsResponse.PostItem> items = posts.stream()
			.map(p -> GetPostsResponse.ofEntity(p, liked.contains(p.getId())))
			.toList();

		// 더 가져올 게 없으니 hasNext=false
		return GetPostsResponse.builder()
			.hasNext(false)
			.posts(items)
			.build();
	}

}
