package org.glue.glue_be.user.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.user.dto.request.UpdateLanguageRequest;
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

		return new LanguageLevelResponse(
			user.getLanguageMain(),
			user.getLanguageMainLevel(),
			user.getLanguageLearn(),
			user.getLanguageLearnLevel()
		);

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

	// 6. 프사 변경

	// 7. 학과 공개여부

	// 8. 모임 히스토리 공개여부

	// 9. 좋아요 목록 공개여부

	// 10. 방명록 공개여부


	// 11. 모임 히스토리 조회

	// 12. 좋아요 목록 조회


	public User getUserById(Long userId) {
		return userRepository.findById(userId).orElseThrow(
			() -> new BaseException(UserResponseStatus.USER_NOT_FOUND)
		);
	}



}
