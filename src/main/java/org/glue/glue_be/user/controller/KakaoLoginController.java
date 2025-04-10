package org.glue.glue_be.user.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.common.response.BaseResponse;
import org.glue.glue_be.user.dto.request.KakaoSignInRequestDto;
import org.glue.glue_be.user.dto.response.KakaoSignInResponseDto;
import org.glue.glue_be.user.dto.request.KakaoSignUpRequestDto;
import org.glue.glue_be.user.dto.response.KakaoSignUpResponseDto;
import org.glue.glue_be.user.dto.response.KakaoUserInfoResponseDto;
import org.glue.glue_be.user.service.AuthService;
import org.glue.glue_be.user.service.KakaoService;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/kakao")
public class KakaoLoginController {

	private final AuthService authService;
	private final KakaoService kakaoService;

	// 회원가입
	@PostMapping("/signup")
	public BaseResponse<KakaoSignUpResponseDto> kakaoSignUp(@RequestBody KakaoSignUpRequestDto requestDto){
		KakaoSignUpResponseDto responseDto = authService.kakaoSignUp(requestDto);
		return new BaseResponse<>(responseDto);
	}


	// 로그인
	@PostMapping("/signin")
	public BaseResponse<KakaoSignInResponseDto> kakaoSignIn(@RequestBody KakaoSignInRequestDto requestDto){
		KakaoSignInResponseDto responseDto = authService.kakaoSignIn(requestDto);

		return new BaseResponse<>(responseDto);
	}

}