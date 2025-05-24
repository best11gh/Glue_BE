package org.glue.glue_be.user.dto.response;

import org.glue.glue_be.user.entity.User;

import java.time.LocalDate;

public record TargetProfileResponse(

	Long userId,
	String profileImageUrl,
	String nickName,
	String description,
	Integer gender,
	LocalDate birthDate,
	Integer school,

	Integer mainLanguage,
	Integer mainLanguageLevel,

	Integer learningLanguage,
	Integer learningLanguageLevel,

	Integer major  // 공개여부 1일 때만 값이 들어감

) {
	public static TargetProfileResponse fromUser(User user) {
		return new TargetProfileResponse(
			user.getUserId(),
			user.getProfileImageUrl(),
			user.getNickname(),
			user.getDescription(),
			user.getGender(),
			user.getBirthDate(),
			user.getSchool(),
			user.getLanguageMain(),
			user.getLanguageMainLevel(),
			user.getLanguageLearn(),
			user.getLanguageLearnLevel(),
			// major 공개여부가 1인 경우에만 설정, 아니면 null
			user.getMajorVisibility() == User.VISIBILITY_PUBLIC ? user.getMajor() : null
		);
	}
}