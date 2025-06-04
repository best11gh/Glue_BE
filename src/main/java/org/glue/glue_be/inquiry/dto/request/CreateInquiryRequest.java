package org.glue.glue_be.inquiry.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record CreateInquiryRequest(

	@NotBlank(message = "제목은 필수값입니다")
	String title,

	@NotNull(message = "문의 유형은 필수값입니다")
	Integer inquiryType,

	@NotNull(message = "내용은 필수값입니다")
	String content,

	@Email
	@NotBlank(message = "응답받을 이메일은 필수값입니다")
	String responseEmail

) {}
