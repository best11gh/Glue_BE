package org.glue.glue_be.auth.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.glue.glue_be.auth.service.AuthService;
import org.glue.glue_be.common.response.BaseResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "인증 및 권한 API")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/code")
	@Operation(summary = "이메일 인증 코드 전송")
	BaseResponse<Void> sendCode(@RequestParam("email") @Email(message = "유효한 이메일 형식이 아닙니다") String email) {
		authService.sendCode(email);
		return new BaseResponse<>();
	}

	@GetMapping("/verify-code")
	@Operation(summary = "이메일 인증 코드 확인")
	BaseResponse<Boolean> verifyCode(
		@RequestParam("email") @Email(message = "유효한 이메일 형식이 아닙니다") String email,
		@RequestParam("code") String code) {
		authService.verifyCode(email, code);
		return new BaseResponse<>(true);
	}

}
