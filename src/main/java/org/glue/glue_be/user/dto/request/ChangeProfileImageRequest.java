package org.glue.glue_be.user.dto.request;


import jakarta.validation.constraints.NotBlank;


public record ChangeProfileImageRequest(
	@NotBlank(message = "프사 변경엔 s3에 업로드 후 publicUrl이 필수 입력값입니다")
	String profileImageUrl
) {}
