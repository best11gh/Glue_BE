package org.glue.glue_be.user.dto.response;

import org.glue.glue_be.user.entity.User;

import java.time.LocalDate;

public record MyProfileResponse (

	Long userId,

	String profileImageUrl,

	String description,

	String realName,

	String nickName,

	LocalDate birthDate,

	Integer gender,

	Integer systemLanguage,

	Integer school,

	Integer major,

	String email

) {
	public static MyProfileResponse fromUser(User user) {
		return new MyProfileResponse(
			user.getUserId(),
			user.getProfileImageUrl(),
			user.getDescription(),
			user.getRealName(),
			user.getNickname(),
			user.getBirthDate(),
			user.getGender(),
			user.getSystemLanguage(),
			user.getSchool(),
			user.getMajor(),
			user.getEmail()
		);
	}
}
