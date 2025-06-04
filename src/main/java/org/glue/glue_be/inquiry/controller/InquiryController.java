package org.glue.glue_be.inquiry.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.glue.glue_be.auth.jwt.CustomUserDetails;
import org.glue.glue_be.common.response.BaseResponse;
import org.glue.glue_be.inquiry.dto.request.CreateInquiryRequest;
import org.glue.glue_be.inquiry.service.InquiryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inquiry")
@Tag(name = "Inquiry", description = "문의 API")
public class InquiryController {

	private final InquiryService inquiryService;

	@PostMapping
	@Operation(summary = "문의 전송하기")
	public BaseResponse<Void> createInquiry(@Valid @RequestBody CreateInquiryRequest request, @AuthenticationPrincipal CustomUserDetails auth) {
		inquiryService.createInquiry(request, auth.getUserId());
		return new BaseResponse<>();
	}
}
