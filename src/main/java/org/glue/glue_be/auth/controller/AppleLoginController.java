package org.glue.glue_be.auth.controller;

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
public class AppleLoginController {

    private final AuthService authService;


    @PostMapping("/signup")
    public BaseResponse<AppleSignUpResponseDto> appleSignUp(@Valid @RequestBody AppleSignUpRequestDto requestDto) {
        AppleSignUpResponseDto responseDto = authService.appleSignUp(requestDto);
        return new BaseResponse<>(responseDto);
    }

    @PostMapping("/signin")
    public BaseResponse<AppleSignInResponseDto> appleSignIn(@Valid @RequestBody AppleSignInRequestDto requestDto) {
        AppleSignInResponseDto responseDto = authService.appleSignIn(requestDto);
        return new BaseResponse<>(responseDto);
    }

}
