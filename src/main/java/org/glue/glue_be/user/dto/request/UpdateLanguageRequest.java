package org.glue.glue_be.user.dto.request;


import jakarta.validation.constraints.NotNull;


public record UpdateLanguageRequest(

	@NotNull(message = "언어는 필수 입력값입니다")
	Integer language,

	@NotNull(message = "언어 수준은 필수 입력값입니다")
	Integer languageLevel

) {}
