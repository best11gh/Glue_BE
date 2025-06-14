package org.glue.glue_be.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenInfoResponseDto(

        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("expires_in")
        int expiresIn,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("scope")
        String scope,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("id_token")
        String idToken

) {
}
