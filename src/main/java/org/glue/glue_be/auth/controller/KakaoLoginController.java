package org.glue.glue_be.auth.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.auth.dto.response.*;
import org.glue.glue_be.common.response.BaseResponse;
import org.glue.glue_be.auth.dto.request.*;
import org.glue.glue_be.auth.service.AuthService;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/kakao")
@Tag(name = "Kakao Auth", description = "카카오 API")
public class KakaoLoginController {

	private final AuthService authService;

	// 회원가입
	@PostMapping("/signup")
	@Operation(summary = "카카오 회원가입")
	public BaseResponse<SignUpResponseDto> kakaoSignUp(@RequestBody @Valid KakaoSignUpRequestDto requestDto){
		SignUpResponseDto responseDto = authService.kakaoSignUp(requestDto);
		return new BaseResponse<>(responseDto);
	}


	// 로그인
	@PostMapping("/signin")
	@Operation(summary = "카카오 로그인")
	public BaseResponse<SignInResponseDto> kakaoSignIn(@RequestBody @Valid KakaoSignInRequestDto requestDto){
		SignInResponseDto responseDto = authService.kakaoSignIn(requestDto);
		return new BaseResponse<>(responseDto);
	}

}