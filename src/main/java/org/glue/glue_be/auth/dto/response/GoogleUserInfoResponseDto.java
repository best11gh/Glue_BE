package org.glue.glue_be.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleUserInfoResponseDto(

        @JsonProperty("id")
        String id

) {
}