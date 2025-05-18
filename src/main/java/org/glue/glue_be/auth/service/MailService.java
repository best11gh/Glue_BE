package org.glue.glue_be.auth.service;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class MailService {

	@Value("${spring.mail.username}")
	private String from;


	private final JavaMailSender mailSender;


	public void sendCodeEmail(String email, String code) {
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(email);
			message.setFrom(from);
			message.setSubject("Glue 서비스 인증 코드 안내");
			message.setText(
				String.format(
					"안녕하세요,\n\n" +
						"요청하신 인증 코드는 다음과 같습니다:\n\n" +
						"%s\n\n" +
						"해당 코드는 5분 동안만 유효합니다.\n\n" +
						"감사합니다.", code
				)
			);

			mailSender.send(message);
		} catch (MailSendException e) {
			throw new MailSendException("email 코드 전송에 실패했습니다", e);
		}
	}



}
