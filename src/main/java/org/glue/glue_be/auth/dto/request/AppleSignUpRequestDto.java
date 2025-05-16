package org.glue.glue_be.auth.dto.request;

import jakarta.validation.constraints.*;
import org.glue.glue_be.user.entity.User;

import java.time.LocalDate;


public record AppleSignUpRequestDto(

    @NotBlank(message = "Authorization code는 필수 입력값입니다.")
    String authorizationCode,

    @NotBlank(message = "이름은 필수 입력값입니다.")
    String userName,

    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    String nickname,

    @NotNull(message = "성별은 필수 입력값입니다.")
    Integer gender,

    @NotNull(message = "생년월일은 필수 입력값입니다.")
    @Past(message = "생년월일은 과거 날짜여야 합니다.")
    LocalDate birthDate,

    @NotNull(message = "국가는 필수 입력값입니다.")
    Integer nation,

    String description,

    @NotNull(message = "전공은 필수 입력값입니다.")
    Integer major,

    @NotNull(message = "전공 노출 여부는 필수 입력값입니다.")
    Integer majorVisibility,

    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    String email,

    Integer school,

    String profileImageUrl,

    Integer systemLanguage,
    Integer languageMain,
    Integer languageMainLevel,
    Integer languageLearn,
    Integer languageLearnLevel,

    Integer meetingVisibility,
    Integer likeVisibility,
    Integer guestbooksVisibility
) {

    // Apple의 경우 oauthId(subject)를 서비스에서 받아 사용!
    public User toEntity(String oauthId) {
        return User.builder()
            .oauthId(oauthId)
            .nickname(nickname())
            .gender(gender())
            .birthDate(birthDate())
            .description(description())
            .major(major())
            .majorVisibility(majorVisibility())
            .email(email())
            .school(school())
            .systemLanguage(systemLanguage())
            .languageMain(languageMain())
            .languageMainLevel(languageMainLevel())
            .languageLearn(languageLearn())
            .languageLearnLevel(languageLearnLevel())
            .profileImageUrl(profileImageUrl())
            .meetingVisibility(meetingVisibility())
            .likeVisibility(likeVisibility())
            .guestbooksVisibility(guestbooksVisibility())
            .build();
    }
}