package org.glue.glue_be.auth.controller;


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
public class AuthController {

	private final AuthService authService;

	@PostMapping("/code")
	BaseResponse<Void> sendCode(@RequestParam("email") @Email(message = "유효한 이메일 형식이 아닙니다") String email) {
		authService.sendCode(email);
		return new BaseResponse<>();
	}

	@GetMapping("/verify-code")
	BaseResponse<Boolean> verifyCode(
		@RequestParam("email") @Email(message = "유효한 이메일 형식이 아닙니다") String email,
		@RequestParam("code") String code) {
		authService.verifyCode(email, code);
		return new BaseResponse<>(true);
	}

}
