package org.glue.glue_be.user.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.glue.glue_be.auth.jwt.CustomUserDetails;
import org.glue.glue_be.common.response.BaseResponse;
import org.glue.glue_be.user.dto.request.ChangeProfileImageRequest;
import org.glue.glue_be.user.dto.request.ChangeSystemLanguageRequest;
import org.glue.glue_be.user.dto.request.UpdateLanguageRequest;
import org.glue.glue_be.user.dto.response.LanguageLevelResponse;
import org.glue.glue_be.user.dto.response.MyProfileResponse;
import org.glue.glue_be.user.dto.response.TargetProfileResponse;
import org.glue.glue_be.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;


	// 1. 내 교환언어/수준 조회
	@GetMapping("/languages")
	public BaseResponse<LanguageLevelResponse> getMyLanguages(
		@AuthenticationPrincipal CustomUserDetails auth
	) {
		LanguageLevelResponse response = userService.getMyLanguages(auth.getUserId());
		return new BaseResponse<>(response);
	}

	//	// 2-1. 내 언어/수준 변경
	@PutMapping("/main-languages")
	public BaseResponse<Void> updateMainLanguages(
		@AuthenticationPrincipal CustomUserDetails auth,
		@Valid @RequestBody UpdateLanguageRequest request
	) {
		userService.updateMainLanguage(auth.getUserId(), request);
		return new BaseResponse<>();
	}

	//	// 2-2. 학습 언어/수준 변경
	@PutMapping("/learning-languages")
	public BaseResponse<Void> updateLearningLanguages(
		@AuthenticationPrincipal CustomUserDetails auth,
		@Valid @RequestBody UpdateLanguageRequest request
	) {
		userService.updateLearningLanguage(auth.getUserId(), request);
		return new BaseResponse<>();
	}

	//	// 3. 프로필 조회 (본인)
	@GetMapping("/profile/me")
	public BaseResponse<MyProfileResponse> getMyProfile( @AuthenticationPrincipal CustomUserDetails auth ) {
		MyProfileResponse response = userService.getMyProfile(auth.getUserId());
		return new BaseResponse<>(response);
	}

	// 4. 프로필 조회 (타인)
	@GetMapping("/profile/{userId}")
	public BaseResponse<TargetProfileResponse> getUserProfile(
		@PathVariable Long userId, @AuthenticationPrincipal CustomUserDetails auth
	) {
		 TargetProfileResponse response = userService.getTargetProfile(userId);
		return new BaseResponse<>(response);
	}

	// 5. 시스템 언어 변경
	@PutMapping("/system-language")
	public BaseResponse<Void> changeSystemLanguage(
		@AuthenticationPrincipal CustomUserDetails auth,
		@Valid @RequestBody ChangeSystemLanguageRequest request
	) {
		userService.changeSystemLanguage(auth.getUserId(), request);
		return new BaseResponse<>();
	}

	// 6. 프로필 사진 변경
	@PutMapping("/profile-image")
	public BaseResponse<Void> updateProfileImage(
		@AuthenticationPrincipal CustomUserDetails auth, @RequestBody ChangeProfileImageRequest request
	) {
		userService.updateProfileImage(auth.getUserId(), request);
		return new BaseResponse<>();
	}
//
//	// 7. 학과 공개여부 설정
//	@PutMapping("/major-visibility")
//	public BaseResponse<Void> setMajorVisibility(
//		@AuthenticationPrincipal CustomUserDetails auth,
//		@RequestParam boolean visible
//	) {
//		userService.setMajorVisibility(auth.getUserId(), visible);
//		return new BaseResponse<>();
//	}
//
//	// 8. 모임 히스토리 공개여부 설정
//	@PutMapping("/meeting-history-visibility")
//	public BaseResponse<Void> setMeetingHistoryVisibility(
//		@AuthenticationPrincipal CustomUserDetails auth,
//		@RequestParam boolean visible
//	) {
//		userService.setMeetingHistoryVisibility(auth.getUserId(), visible);
//		return new BaseResponse<>();
//	}
//
//	// 9. 좋아요 목록 공개여부 설정
//	@PutMapping("/like-list-visibility")
//	public BaseResponse<Void> setLikeListVisibility(
//		@AuthenticationPrincipal CustomUserDetails auth,
//		@RequestParam boolean visible
//	) {
//		userService.setLikeListVisibility(auth.getUserId(), visible);
//		return new BaseResponse<>();
//	}
//
//	// 10. 방명록 공개여부 설정
//	@PutMapping("/guestbook-visibility")
//	public BaseResponse<Void> setGuestbookVisibility(
//		@AuthenticationPrincipal CustomUserDetails auth,
//		@RequestParam boolean visible
//	) {
//		userService.setGuestbookVisibility(auth.getUserId(), visible);
//		return new BaseResponse<>();
//	}
//
//	// 11. 모임 히스토리 조회
//	@GetMapping("/meetings-history")
//	public BaseResponse<MeetingHistoryResponse[]> getMeetingHistory(
//		@AuthenticationPrincipal CustomUserDetails auth,
//		@RequestParam(required = false) Long cursorId,
//		@RequestParam(defaultValue = "10") int size
//	) {
//		MeetingHistoryResponse[] resp = userService.getMeetingHistory(auth.getUserId(), cursorId, size);
//		return new BaseResponse<>(resp);
//	}
//
//	// 12. 좋아요 목록 조회
//	@GetMapping("/likes")
//	public BaseResponse<LikeListResponse[]> getLikeList(
//		@AuthenticationPrincipal CustomUserDetails auth,
//		@RequestParam(required = false) Long cursorId,
//		@RequestParam(defaultValue = "10") int size
//	) {
//		LikeListResponse[] resp = userService.getLikeList(auth.getUserId(), cursorId, size);
//		return new BaseResponse<>(resp);
//	}




}
