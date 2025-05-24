package org.glue.glue_be.user.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.post.dto.response.GetLikedPostsResponse;
import org.glue.glue_be.post.entity.Like;
import org.glue.glue_be.post.entity.Post;
import org.glue.glue_be.post.repository.LikeRepository;
import org.glue.glue_be.post.repository.PostRepository;
import org.glue.glue_be.user.dto.request.ChangeProfileImageRequest;
import org.glue.glue_be.user.dto.request.ChangeSystemLanguageRequest;
import org.glue.glue_be.user.dto.request.UpdateLanguageRequest;
import org.glue.glue_be.user.dto.response.*;
import org.glue.glue_be.user.entity.User;
import org.glue.glue_be.user.repository.UserRepository;
import org.glue.glue_be.user.response.UserResponseStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

	private final UserRepository userRepository;
	private final LikeRepository likeRepository;
	private final PostRepository postRepository;


	// 1. 내 교환언어/수준 조회
	@Transactional(readOnly = true)
	public LanguageLevelResponse getMyLanguages(Long userId) {
		User user = getUserById(userId);
		return LanguageLevelResponse.from(user);
	}


	// 2-1. 내 교환언어/수준 변경
	public void updateMainLanguage(Long userId, UpdateLanguageRequest updateLanguageRequest) {
		User user = getUserById(userId);
		user.changeLanguageMain(updateLanguageRequest.language());
		user.changeLanguageMainLevel(updateLanguageRequest.languageLevel());
	}

	// 2-1. 내 학습언어/수준 변경
	public void updateLearningLanguage(Long userId, UpdateLanguageRequest updateLanguageRequest) {
		User user = getUserById(userId);
		user.changeLanguageLearn(updateLanguageRequest.language());
		user.changeLanguageLearnLevel(updateLanguageRequest.languageLevel());
	}

	// 3. 프로필 조회 (본인)
	public MyProfileResponse getMyProfile(Long userId) {
		User user = getUserById(userId);
		return MyProfileResponse.fromUser(user);
	}

	// 4. 프로필 조회 (타인)
	public TargetProfileResponse getTargetProfile(Long userId) {
		User user = getUserById(userId);
		return TargetProfileResponse.fromUser(user);
	}

	// 5. 시스템 언어 변경
	public void changeSystemLanguage(Long userId, ChangeSystemLanguageRequest request) {
		User user = getUserById(userId);
		user.changeSystemLanguage(request.systemLanguage());
	}

	// 6. 프사 변경
	public void changeProfileImage(Long userId, ChangeProfileImageRequest request) {
		User user = getUserById(userId);
		user.changeProfileImageUrl(request.profileImageUrl());
	}


	// 7. 학과 공개여부
	public void setMajorVisibility(Long userId, int currentVisible) {
		User user = getUserById(userId);

		if(currentVisible == User.VISIBILITY_PUBLIC) user.changeMajorVisibility(User.VISIBILITY_PRIVATE);
		else if(currentVisible == User.VISIBILITY_PRIVATE) user.changeMajorVisibility(User.VISIBILITY_PUBLIC);
		else throw new RuntimeException("현재 visibie 값은 0 또는 1만 가능합니다");
	}


	// 8. 모임 히스토리 공개여부
	public void setMeetingHistoryVisibility(Long userId, int currentVisible) {
		User user = getUserById(userId);

		if(currentVisible == User.VISIBILITY_PUBLIC) user.changeMeetingVisibility(User.VISIBILITY_PUBLIC);
		else if(currentVisible == User.VISIBILITY_PRIVATE) user.changeMeetingVisibility(User.VISIBILITY_PRIVATE);
		else throw new RuntimeException("현재 visibie 값은 0 또는 1만 가능합니다");
	}

	// 9. 좋아요 목록 공개여부
	public void setLikeListVisibility(Long userId, int currentVisible) {
		User user = getUserById(userId);

		if(currentVisible == User.VISIBILITY_PUBLIC) user.changeLikeVisibility(User.VISIBILITY_PRIVATE);
		else if(currentVisible == User.VISIBILITY_PRIVATE) user.changeLikeVisibility(User.VISIBILITY_PUBLIC);
		else throw new RuntimeException("현재 visibie 값은 0 또는 1만 가능합니다");
	}

	// 10. 방명록 공개여부
	public void setGuestbookVisibility(Long userId, int currentVisible) {
		User user = getUserById(userId);

		if(currentVisible == User.VISIBILITY_PUBLIC) user.changeGuestbooksVisibility(User.VISIBILITY_PRIVATE);
		else if(currentVisible == User.VISIBILITY_PRIVATE) user.changeGuestbooksVisibility(User.VISIBILITY_PUBLIC);
		else throw new RuntimeException("현재 visibie 값은 0 또는 1만 가능합니다");
	}

	// 11. 공개여부 정보 조회
	public GetVisibilitiesResponse getVisibilities(Long userId) {
		User user = getUserById(userId);
		return GetVisibilitiesResponse.from(user);
	}


	// 12. 모임 히스토리 목록 조회
	public MeetingHistoryResponse getMeetingHistory(Long myUserId, Long targetUserId) {

		// 1. 일단 히스토리 조회할 유저 정보 가져오기
		User user = getUserById(targetUserId);

		// 2. 공개여부 확인(본인 확인은 그냥 진행)
		if(myUserId != targetUserId && user.getMeetingVisibility() == User.VISIBILITY_PRIVATE)
			throw new BaseException(UserResponseStatus.NOT_OPEN);

		// 3. dto 조립

		// 호스트/참가모임 조회
		List<Post> hostedPosts = postRepository.findByHostUserId(targetUserId);
		List<Post> joinedPosts = postRepository.findByParticipantUserIdExcludingHost(targetUserId);

		// dto 변환하는 팩토리 메서드에 POST를 넣는다. 현 연관관계에선 post는 meeting을 참고할 수 있지만 역은 안되기 때문
		List<MeetingHistoryResponse.MeetingHistoryItem> hostedMeetings = hostedPosts.stream()
			.map(MeetingHistoryResponse::convertToMeetingHistoryItem)
			.collect(Collectors.toList());

		List<MeetingHistoryResponse.MeetingHistoryItem> joinedMeetings = joinedPosts.stream()
			.map(MeetingHistoryResponse::convertToMeetingHistoryItem)
			.collect(Collectors.toList());

		return MeetingHistoryResponse.builder()
			.hostedMeetings(hostedMeetings)
			.joinedMeetings(joinedMeetings)
			.build();
	}

	// 13. 좋아요 목록조회
	public GetLikedPostsResponse getLikeList(Long myUserId, Long targetUserId) {

		// 1. 일단 좋아요 목록 조회할 유저 정보 가져오고
		User user = getUserById(targetUserId);

		// 2. 공개여부 확인(본인 확인은 그냥 진행)
		if(myUserId != targetUserId && user.getLikeVisibility() == User.VISIBILITY_PRIVATE)
			throw new BaseException(UserResponseStatus.NOT_OPEN);

		// 3. dto 조립
		List<Like> likeList = likeRepository.findByUser_UserIdOrderByCreatedAtDesc(targetUserId);

		// likeList를 stream으로 하나씩 순회해 개별 순회자 like마다 getPost()를 ofEntity으로 변환한 값으로 리턴
		List<GetLikedPostsResponse.PostItem> postItems = likeList.stream()
			.map(like -> GetLikedPostsResponse.ofEntity(like.getPost()))
			.toList();

		return GetLikedPostsResponse.builder()
			.posts(postItems)
			.build();
	}

	// 14. 한줄 소개 변경
	public void updateDescription(Long userId, String description) {
		User user = getUserById(userId);
		user.changeDescription(description);
	}



	// 공용: 공용 유저 조회 메서드
	public User getUserById(Long userId) {
		return userRepository.findById(userId).orElseThrow(
			() -> new BaseException(UserResponseStatus.USER_NOT_FOUND)
		);
	}




}
