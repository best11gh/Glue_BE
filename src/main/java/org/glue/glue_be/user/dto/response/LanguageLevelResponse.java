package org.glue.glue_be.user.dto.response;

public record LanguageLevelResponse(

	Integer mainLanguage,

	Integer mainLanguageLevel,

	Integer learningLanguage,

	Integer learningLanguageLevel
) {}