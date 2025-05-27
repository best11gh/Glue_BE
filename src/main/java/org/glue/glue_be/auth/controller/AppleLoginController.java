package org.glue.glue_be.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.common.response.BaseResponse;
import org.glue.glue_be.auth.dto.request.AppleSignInRequestDto;
import org.glue.glue_be.auth.dto.request.AppleSignUpRequestDto;
import org.glue.glue_be.auth.dto.response.AppleSignInResponseDto;
import org.glue.glue_be.auth.dto.response.AppleSignUpResponseDto;
import org.glue.glue_be.auth.service.AuthService;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/apple")
@Tag(name = "Apple Auth", description = "애플 API")
public class AppleLoginController {

    private final AuthService authService;


    @PostMapping("/signup")
    @Operation(summary = "애플 회원가입")
    public BaseResponse<AppleSignUpResponseDto> appleSignUp(@Valid @RequestBody AppleSignUpRequestDto requestDto) {
        AppleSignUpResponseDto responseDto = authService.appleSignUp(requestDto);
        return new BaseResponse<>(responseDto);
    }

    @PostMapping("/signin")
    @Operation(summary = "애플 로그인")
    public BaseResponse<AppleSignInResponseDto> appleSignIn(@Valid @RequestBody AppleSignInRequestDto requestDto) {
        AppleSignInResponseDto responseDto = authService.appleSignIn(requestDto);
        return new BaseResponse<>(responseDto);
    }

}
