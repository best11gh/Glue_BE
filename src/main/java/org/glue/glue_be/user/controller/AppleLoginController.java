package org.glue.glue_be.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.common.response.BaseResponse;
import org.glue.glue_be.user.dto.request.AppleSignInRequestDto;
import org.glue.glue_be.user.dto.request.AppleSignUpRequestDto;
import org.glue.glue_be.user.dto.response.AppleSignInResponseDto;
import org.glue.glue_be.user.dto.response.AppleSignUpResponseDto;
import org.glue.glue_be.user.service.AuthService;
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
