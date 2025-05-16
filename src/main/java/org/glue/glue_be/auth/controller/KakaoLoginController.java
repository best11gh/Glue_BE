package org.glue.glue_be.auth.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.common.response.BaseResponse;
import org.glue.glue_be.auth.dto.request.KakaoSignInRequestDto;
import org.glue.glue_be.auth.dto.response.KakaoSignInResponseDto;
import org.glue.glue_be.auth.dto.request.KakaoSignUpRequestDto;
import org.glue.glue_be.auth.dto.response.KakaoSignUpResponseDto;
import org.glue.glue_be.auth.service.AuthService;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/kakao")
public class KakaoLoginController {

	private final AuthService authService;

	// 회원가입
	@PostMapping("/signup")
	public BaseResponse<KakaoSignUpResponseDto> kakaoSignUp(@RequestBody @Valid KakaoSignUpRequestDto requestDto){
		KakaoSignUpResponseDto responseDto = authService.kakaoSignUp(requestDto);
		return new BaseResponse<>(responseDto);
	}


	// 로그인
	@PostMapping("/signin")
	public BaseResponse<KakaoSignInResponseDto> kakaoSignIn(@RequestBody @Valid KakaoSignInRequestDto requestDto){
		KakaoSignInResponseDto responseDto = authService.kakaoSignIn(requestDto);

		return new BaseResponse<>(responseDto);
	}

}