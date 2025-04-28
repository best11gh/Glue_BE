package org.glue.glue_be.user.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record AppleSignUpRequestDto(
        @NotBlank(message = "Authorization code는 필수 입력값입니다.")
        String authorizationCode,

        @NotBlank(message = "이름은 필수 입력값입니다.")
        String userName,

        @NotBlank(message = "닉네임은 필수 입력값입니다.")
        String nickName,

        @NotNull(message = "성별은 필수 입력값입니다.")
        Integer gender,

        @NotNull(message = "생년월일은 필수 입력값입니다.")
        @Past(message = "생년월일은 과거 날짜여야 합니다.")
        LocalDate birthDate,

        @NotNull(message = "국가는 필수 입력값입니다.")
        Integer nation,

        @NotBlank(message = "한마디는 필수 입력값입니다.")
        String description,

        @NotNull(message = "전공은 필수 입력값입니다.")
        Integer major,

        @NotNull(message = "전공 노출 여부는 필수 입력값입니다.")
        Integer majorVisibility
) {}
