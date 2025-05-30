package org.glue.glue_be.user.entity;

import jakarta.persistence.*;
import java.time.*;

import lombok.*;
import org.glue.glue_be.common.BaseEntity;
import org.glue.glue_be.common.config.LocalDateStringConverter;


@Getter
@Entity
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    public static final int SYSTEM_LANGUAGE_KOREAN = 1;
    public static final int SYSTEM_LANGUAGE_ENGLISH = 2;

    public static final int VISIBILITY_PUBLIC = 1;
    public static final int VISIBILITY_PRIVATE = 0;

    public static final int IS_NOT_DELETED = 0;
    public static final int IS_DELETED = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "oauth_id", nullable = false, unique = true)
    private String oauthId;

    @Column(name = "real_name", nullable = false)
    private String realName;

    @Column(name = "nickname", nullable = false, length = 10, unique = true)
    private String nickname;

    @Column(name = "gender", nullable = false)
    private Integer gender;

    @Column(name = "birth_date", nullable = false)
    @Convert(converter = LocalDateStringConverter.class)
    private LocalDate birthDate;

    @Column(name = "description", length = 50)
    private String description;

    @Column(name = "major", nullable = false)
    private Integer major;

    @Column(name = "school", nullable = false) // default = 272
    private Integer school;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "language_main", nullable = false) // default = 1
    private Integer languageMain;

    @Column(name = "language_main_level", nullable = false) // default = 3
    private Integer languageMainLevel;

    @Column(name = "language_learn", nullable = false) // default = 2
    private Integer languageLearn;

    @Column(name = "language_learn_level", nullable = false) // default = 3
    private Integer languageLearnLevel;

    @Column(name = "system_language", nullable = false) // default = 1
    private Integer systemLanguage;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Column(name = "major_visibility", nullable = false) // default = 1
    private Integer majorVisibility;

    @Column(name = "meeting_visibility", nullable = false) // default = 1
    private Integer meetingVisibility;

    @Column(name = "like_visibility", nullable = false) // default = 1
    private Integer likeVisibility;

    @Column(name = "guestbooks_visibility", nullable = false) // default = 1
    private Integer guestbooksVisibility;

    @Column(name = "is_deleted", nullable = false)
    private Integer isDeleted;


    @Builder
    public User(String oauthId, String realName, String nickname, Integer gender, LocalDate birthDate, String description, Integer major, Integer school, String email, Integer systemLanguage, Integer languageMain, Integer languageLearn, Integer languageMainLevel, Integer languageLearnLevel, String profileImageUrl, Integer majorVisibility, Integer meetingVisibility, Integer likeVisibility, Integer guestbooksVisibility) {
        this.oauthId = oauthId;
        this.realName = realName;
        this.nickname = nickname;
        this.gender = gender;
        this.birthDate = birthDate;
        this.description = description;
        this.major = major;
        this.school = (school == null) ? 272 : school;
        this.email = email;
        this.systemLanguage = (systemLanguage == null) ? SYSTEM_LANGUAGE_KOREAN : systemLanguage;
        this.languageMain = (languageMain == null) ? 1 : languageMain;
        this.languageLearn = (languageLearn == null) ? 2 : languageLearn;
        this.languageMainLevel = (languageMainLevel == null) ? 3 : languageMainLevel;
        this.languageLearnLevel = (languageLearnLevel == null) ? 3 : languageLearnLevel;
        this.profileImageUrl = profileImageUrl;
        this.majorVisibility = (majorVisibility == null) ? VISIBILITY_PUBLIC : majorVisibility;
        this.meetingVisibility = (meetingVisibility == null) ? VISIBILITY_PUBLIC : meetingVisibility;
        this.likeVisibility = (likeVisibility == null) ? VISIBILITY_PUBLIC : likeVisibility;
        this.guestbooksVisibility = (guestbooksVisibility == null) ? VISIBILITY_PUBLIC : guestbooksVisibility;
        this.isDeleted = IS_NOT_DELETED;
    }


    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    public void changeFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }


    public void changeProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }


    public void changeLanguageMain(Integer languageMain) {
        this.languageMain = languageMain;
    }


    public void changeLanguageMainLevel(Integer languageMainLevel) {
        this.languageMainLevel = languageMainLevel;
    }


    public void changeLanguageLearn(Integer languageLearn) {
        this.languageLearn = languageLearn;
    }


    public void changeLanguageLearnLevel(Integer languageLearnLevel) {
        this.languageLearnLevel = languageLearnLevel;
    }


    public void changeSystemLanguage(Integer systemLanguage) {
        this.systemLanguage = systemLanguage;
    }


    public void changeMajorVisibility(Integer majorVisibility) {
        this.majorVisibility = majorVisibility;
    }


    public void changeMeetingVisibility(Integer meetingVisibility) {
        this.meetingVisibility = meetingVisibility;
    }


    public void changeLikeVisibility(Integer likeVisibility) {
        this.likeVisibility = likeVisibility;
    }


    public void changeGuestbooksVisibility(Integer guestbooksVisibility) {
        this.guestbooksVisibility = guestbooksVisibility;
    }

    public void anonymizeForSignOut() {
        // 탈퇴
        this.isDeleted = IS_DELETED;

        // not null 필드들을 기본값으로 설정
        this.birthDate = LocalDate.parse("1900-01-01T00:00:00");
        this.email = "deleted@deleted.com";
        this.gender = -1;
        this.guestbooksVisibility = VISIBILITY_PRIVATE;
        this.languageLearn = -1;
        this.languageLearnLevel = -1;
        this.languageMain = -1;
        this.languageMainLevel = -1;
        this.likeVisibility = VISIBILITY_PRIVATE;
        this.major = -1;
        this.majorVisibility = -1;
        this.meetingVisibility = -1;
        this.nickname = "탈퇴한 사용자_" + this.userId;
        this.oauthId = "deleted_oauth_id_" + this.userId;
        this.school = -1;
        this.systemLanguage = -1;

        // nullable 필드들을 null로 설정
        this.description = null;
        this.fcmToken = null;
        this.profileImageUrl = null;
        this.realName = null;
    }
}
