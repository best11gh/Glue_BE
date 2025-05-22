package org.glue.glue_be.user.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.user.dto.request.ChangeProfileImageRequest;
import org.glue.glue_be.user.dto.request.ChangeSystemLanguageRequest;
import org.glue.glue_be.user.dto.request.UpdateLanguageRequest;
import org.glue.glue_be.user.dto.response.GetVisibilitiesResponse;
import org.glue.glue_be.user.dto.response.LanguageLevelResponse;
import org.glue.glue_be.user.dto.response.MyProfileResponse;
import org.glue.glue_be.user.dto.response.TargetProfileResponse;
import org.glue.glue_be.user.entity.User;
import org.glue.glue_be.user.repository.UserRepository;
import org.glue.glue_be.user.response.UserResponseStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

	private final UserRepository userRepository;


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


	// 8. 모임 히스토리
	public void setMeetingHistoryVisibility(Long userId, int currentVisible) {
		User user = getUserById(userId);

		if(currentVisible == User.VISIBILITY_PUBLIC) user.changeMeetingVisibility(User.VISIBILITY_PUBLIC);
		else if(currentVisible == User.VISIBILITY_PRIVATE) user.changeMeetingVisibility(User.VISIBILITY_PRIVATE);
		else throw new RuntimeException("현재 visibie 값은 0 또는 1만 가능합니다");
	}

	// 9. 좋아요 목록 공개여부
	// todo: 좋아요 목록 조회시 확인
	public void setLikeListVisibility(Long userId, int currentVisible) {
		User user = getUserById(userId);

		if(currentVisible == User.VISIBILITY_PUBLIC) user.changeLikeVisibility(User.VISIBILITY_PRIVATE);
		else if(currentVisible == User.VISIBILITY_PRIVATE) user.changeLikeVisibility(User.VISIBILITY_PUBLIC);
		else throw new RuntimeException("현재 visibie 값은 0 또는 1만 가능합니다");
	}

	// 10. 방명록 공개여부
	// todo: 방명록 조회시 확인
	public void setGuestbookVisibility(Long userId, int currentVisible) {
		User user = getUserById(userId);

		if(currentVisible == User.VISIBILITY_PUBLIC) user.changeGuestbooksVisibility(User.VISIBILITY_PRIVATE);
		else if(currentVisible == User.VISIBILITY_PRIVATE) user.changeGuestbooksVisibility(User.VISIBILITY_PUBLIC);
		else throw new RuntimeException("현재 visibie 값은 0 또는 1만 가능합니다");
	}

	// 11. 공개여부 정보 조회
	public GetVisibilitiesResponse getVisibilites(Long userId) {
		User user = getUserById(userId);
		return GetVisibilitiesResponse.from(user);
	}


	// 12. 모임 히스토리 조회

	// 13. 좋아요 목록 조회


	public User getUserById(Long userId) {
		return userRepository.findById(userId).orElseThrow(
			() -> new BaseException(UserResponseStatus.USER_NOT_FOUND)
		);
	}



}
