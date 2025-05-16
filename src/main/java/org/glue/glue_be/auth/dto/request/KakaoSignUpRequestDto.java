package org.glue.glue_be.auth.dto.request;

import jakarta.validation.constraints.*;
import org.glue.glue_be.user.entity.User;

import java.time.LocalDate;


// dto에 record를 쓰면 좋은점
// 자체적으로 getter 내장, 불변 객체라 데이터 신뢰성-일관성 보장

public record KakaoSignUpRequestDto(

	@NotBlank(message = "OAuth ID는 필수 입력값입니다.")
	String oauthId,

	@NotBlank(message = "닉네임은 필수 입력값입니다.")
	String nickname,

	@NotNull(message = "성별은 필수 입력값입니다.")
	Integer gender,

	@NotNull(message = "생년월일은 필수 입력값입니다.")
	@Past(message = "생년월일은 과거 날짜여야 합니다.")
	LocalDate birthDate,

	String description,

	@NotNull(message = "전공은 필수 입력값입니다.")
	Integer major,

	@NotNull(message = "전공 노출 여부는 필수 입력값입니다.")
	Integer majorVisibility,

	@NotBlank(message = "이메일은 필수 입력값입니다.")
	@Email(message = "이메일 형식이 올바르지 않습니다.")
	String email,

	Integer school,

	String profileImageUrl,

	Integer systemLanguage,
	Integer languageMain,
	Integer languageMainLevel,
	Integer languageLearn,
	Integer languageLearnLevel,

	Integer meetingVisibility,
	Integer likeVisibility,
	Integer guestbooksVisibility
) {

	// record는 값을 가져오는데에 속성명과 이름이 같은 getter 메서드를 사용!
	public User toEntity() {
		return User.builder()
			.oauthId(oauthId())
			.nickname(nickname())
			.gender(gender())
			.birthDate(birthDate())
			.description(description())
			.major(major())
			.majorVisibility(majorVisibility())
			.email(email())
			.school(school())
			.systemLanguage(systemLanguage())
			.languageMain(languageMain())
			.languageMainLevel(languageMainLevel())
			.languageLearn(languageLearn())
			.languageLearnLevel(languageLearnLevel())
			.profileImageUrl(profileImageUrl())
			.meetingVisibility(meetingVisibility())
			.likeVisibility(likeVisibility())
			.guestbooksVisibility(guestbooksVisibility())
			.build();
	}

}