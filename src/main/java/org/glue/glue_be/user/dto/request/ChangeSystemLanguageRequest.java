package org.glue.glue_be.user.dto.request;


import jakarta.validation.constraints.NotNull;


public record ChangeSystemLanguageRequest(

	@NotNull(message = "시스템 언어 변경 시 시스템 언어는 입력 필수값입니다")
	Integer systemLanguage
) {}
