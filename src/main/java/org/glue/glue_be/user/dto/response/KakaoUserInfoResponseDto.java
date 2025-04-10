package org.glue.glue_be.user.dto.response;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;


@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record KakaoUserInfoResponseDto(
	Long id,
	KakaoAccount kakaoAccount
) {

	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public record KakaoAccount(
		KakaoUserProfile profile
	) {

		@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
		public record KakaoUserProfile(
			String nickname,
			String profileImageUrl
		) {
		}
	}
}

