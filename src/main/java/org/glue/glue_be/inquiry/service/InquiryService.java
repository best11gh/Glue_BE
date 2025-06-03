package org.glue.glue_be.inquiry.service;


import lombok.RequiredArgsConstructor;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.inquiry.dto.request.CreateInquiryRequest;
import org.glue.glue_be.inquiry.entity.Inquiry;
import org.glue.glue_be.inquiry.repository.InquiryRepository;
import org.glue.glue_be.user.entity.User;
import org.glue.glue_be.user.repository.UserRepository;
import org.glue.glue_be.user.response.UserResponseStatus;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class InquiryService {

	private final InquiryRepository inquiryRepository;
	private final UserRepository userRepository;

	public void createInquiry(CreateInquiryRequest request, Long userId) {

		User creator = userRepository.findById(userId).orElseThrow(() -> new BaseException(UserResponseStatus.USER_NOT_FOUND));

		Inquiry inquiry = Inquiry.builder()
			.user(creator)
			.title(request.title())
			.inquiryType(request.inquiryType())
			.content(request.content())
			.responseEmail(request.responseEmail())
			.build();

		inquiryRepository.save(inquiry);

	}

}
