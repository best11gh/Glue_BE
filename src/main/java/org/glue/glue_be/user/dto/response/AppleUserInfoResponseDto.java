package org.glue.glue_be.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Data
public class AppleUserInfoResponseDto {
    // 고유ID
    @JsonProperty("sub")
    private String subject;

    @JsonProperty("email")
    private String email;
}
