package org.glue.glue_be.user.dto.response;


import org.glue.glue_be.user.entity.User;


public record GetMainPageInfoResponse(

	String profileImageUrl,

	String userNickname,

	String description,

	Integer mainLanguage,

	Integer mainLanguageLevel,

	Integer learningLanguage,

	Integer learningLanguageLevel
) {
	public static GetMainPageInfoResponse from(User user) {
		return new GetMainPageInfoResponse(
			user.getProfileImageUrl(),
			user.getNickname(),
			user.getDescription(),
			user.getLanguageMain(),
			user.getLanguageMainLevel(),
			user.getLanguageLearn(),
			user.getLanguageLearnLevel()
		);
	}
}