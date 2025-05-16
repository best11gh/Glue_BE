package org.glue.glue_be.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummary {
    private Long userId;
    private String userNickname;
    private String profileImageUrl;
}