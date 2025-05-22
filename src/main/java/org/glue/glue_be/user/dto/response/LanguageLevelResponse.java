package org.glue.glue_be.user.dto.response;


import org.glue.glue_be.user.entity.User;


public record LanguageLevelResponse(

	Integer mainLanguage,

	Integer mainLanguageLevel,

	Integer learningLanguage,

	Integer learningLanguageLevel
) {
	public static LanguageLevelResponse from(User user) {
		return new LanguageLevelResponse(
			user.getLanguageMain(),
			user.getLanguageMainLevel(),
			user.getLanguageLearn(),
			user.getLanguageLearnLevel()
		);
	}
}