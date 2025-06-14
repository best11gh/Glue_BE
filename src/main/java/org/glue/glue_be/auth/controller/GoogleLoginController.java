package org.glue.glue_be.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.auth.dto.request.GoogleSignInRequestDto;
import org.glue.glue_be.auth.dto.request.GoogleSignUpRequestDto;
import org.glue.glue_be.auth.dto.response.GoogleSignInResponseDto;
import org.glue.glue_be.auth.dto.response.GoogleSignUpResponseDto;
import org.glue.glue_be.auth.service.AuthService;
import org.glue.glue_be.common.response.BaseResponse;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/google")
@Tag(name = "Google Auth", description = "구글 로그인 API")
public class GoogleLoginController {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "구글 회원가입")
    public BaseResponse<GoogleSignUpResponseDto> googleSignUp(@Valid @RequestBody GoogleSignUpRequestDto requestDto) {
        GoogleSignUpResponseDto responseDto = authService.googleSignUp(requestDto);
        return new BaseResponse<>(responseDto);
    }

    @PostMapping("/signin")
    @Operation(summary = "구글 로그인")
    public BaseResponse<GoogleSignInResponseDto> googleSignIn(@Valid @RequestBody GoogleSignInRequestDto requestDto) {
        GoogleSignInResponseDto responseDto = authService.googleSignIn(requestDto);
        return new BaseResponse<>(responseDto);
    }
}
