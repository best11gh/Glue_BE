package org.glue.glue_be.user.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.glue.glue_be.auth.jwt.CustomUserDetails;
import org.glue.glue_be.common.response.BaseResponse;
import org.glue.glue_be.post.dto.response.GetLikedPostsResponse;
import org.glue.glue_be.user.dto.request.ChangeProfileImageRequest;
import org.glue.glue_be.user.dto.request.ChangeSystemLanguageRequest;
import org.glue.glue_be.user.dto.request.UpdateDescriptionRequest;
import org.glue.glue_be.user.dto.request.UpdateLanguageRequest;
import org.glue.glue_be.user.dto.response.*;
import org.glue.glue_be.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;


	// 1. 마이페이지 화면 정보 가져오기 => 닉네임, 한줄소개, 교환언어/수준 조회
	@GetMapping("/my-page")
	public BaseResponse<GetMainPageInfoResponse> getMyPageInfo(@AuthenticationPrincipal CustomUserDetails auth ) {
		GetMainPageInfoResponse response = userService.getMyPageInfo(auth.getUserId());
		return new BaseResponse<>(response);
	}

	//	// 2-1. 내 언어/수준 변경
	@PutMapping("/main-language")
	public BaseResponse<Void> updateMainLanguage(@AuthenticationPrincipal CustomUserDetails auth, @Valid @RequestBody UpdateLanguageRequest request) {
		userService.updateMainLanguage(auth.getUserId(), request);
		return new BaseResponse<>();
	}

	//	// 2-2. 학습 언어/수준 변경
	@PutMapping("/learning-language")
	public BaseResponse<Void> updateLearningLanguage(@AuthenticationPrincipal CustomUserDetails auth, @Valid @RequestBody UpdateLanguageRequest request) {
		userService.updateLearningLanguage(auth.getUserId(), request);
		return new BaseResponse<>();
	}

	//	// 3. 프로필 조회 (본인)
	@GetMapping("/profile/me")
	public BaseResponse<MyProfileResponse> getMyProfile(@AuthenticationPrincipal CustomUserDetails auth) {
		MyProfileResponse response = userService.getMyProfile(auth.getUserId());
		return new BaseResponse<>(response);
	}

	// 4. 프로필 조회 (타인)
	@GetMapping("/profile/{userId}")
	public BaseResponse<TargetProfileResponse> getUserProfile(@PathVariable Long userId, @AuthenticationPrincipal CustomUserDetails auth) {
		 TargetProfileResponse response = userService.getTargetProfile(userId);
		return new BaseResponse<>(response);
	}

	// 5. 시스템 언어 변경
	@PutMapping("/system-language")
	public BaseResponse<Void> changeSystemLanguage(@AuthenticationPrincipal CustomUserDetails auth, @Valid @RequestBody ChangeSystemLanguageRequest request) {
		userService.changeSystemLanguage(auth.getUserId(), request);
		return new BaseResponse<>();
	}

	// 6. 프로필 사진 변경
	@PutMapping("/profile-image")
	public BaseResponse<Void> updateProfileImage(@AuthenticationPrincipal CustomUserDetails auth, @RequestBody ChangeProfileImageRequest request) {
		userService.changeProfileImage(auth.getUserId(), request);
		return new BaseResponse<>();
	}

	// 7. 학과 공개여부 설정
	// 현재 visible 상태를 입력으로 받는다. db엔 !currentVisible을 저장하게됨
	@PutMapping("/major-visibility")
	public BaseResponse<Void> setMajorVisibility(@AuthenticationPrincipal CustomUserDetails auth, @RequestParam int currentVisible) {
		userService.setMajorVisibility(auth.getUserId(), currentVisible);
		return new BaseResponse<>();
	}

	// 8. 모임 히스토리 공개여부 설정
	@PutMapping("/meeting-history-visibility")
	public BaseResponse<Void> setMeetingHistoryVisibility(@AuthenticationPrincipal CustomUserDetails auth, @RequestParam int currentVisible) {
		userService.setMeetingHistoryVisibility(auth.getUserId(), currentVisible);
		return new BaseResponse<>();
	}

	// 9. 좋아요 목록 공개여부 설정
	@PutMapping("/like-list-visibility")
	public BaseResponse<Void> setLikeListVisibility(
		@AuthenticationPrincipal CustomUserDetails auth, @RequestParam int currentVisible) {
		userService.setLikeListVisibility(auth.getUserId(), currentVisible);
		return new BaseResponse<>();
	}

	// 10. 방명록 공개여부 설정
	@PutMapping("/guestbook-visibility")
	public BaseResponse<Void> setGuestbookVisibility(@AuthenticationPrincipal CustomUserDetails auth, @RequestParam int currentVisible) {
		userService.setGuestbookVisibility(auth.getUserId(), currentVisible);
		return new BaseResponse<>();
	}

	// 11. 공개 여부 정보 조회
	@GetMapping("/visibility")
	public BaseResponse<GetVisibilitiesResponse> getVisibilities(@AuthenticationPrincipal CustomUserDetails auth){
		GetVisibilitiesResponse response = userService.getVisibilities(auth.getUserId());
		return new BaseResponse<>(response);
	}

	// 12. 모임 히스토리 조회
	@GetMapping("/meetings-history")
	public BaseResponse<MeetingHistoryResponse> getMeetingHistory(@AuthenticationPrincipal CustomUserDetails auth, @RequestParam Long targetUserId) {
		MeetingHistoryResponse response = userService.getMeetingHistory(auth.getUserId(), targetUserId);
		return new BaseResponse<>(response);
	}

	// 13. 좋아요 목록 조회
	@GetMapping("/likes")
	public BaseResponse<GetLikedPostsResponse> getLikeList(
		@AuthenticationPrincipal CustomUserDetails auth,  @RequestParam Long targetUserId) {
		GetLikedPostsResponse response = userService.getLikeList(auth.getUserId(), targetUserId);
		return new BaseResponse<>(response);
	}

	// 14. 내 한줄소개 수정
	@PostMapping("/description")
	public BaseResponse<Void> updateDescription(@AuthenticationPrincipal CustomUserDetails auth, @RequestBody UpdateDescriptionRequest request) {
		userService.updateDescription(auth.getUserId(), request.description());
		return new BaseResponse<>();
	}




}
