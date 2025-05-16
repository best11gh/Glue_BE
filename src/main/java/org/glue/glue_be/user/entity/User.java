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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "oauth_id", nullable = false, unique = true)
    private String oauthId;

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

    @Column(name = "profileImageUrl")
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


    @Builder
    public User(String oauthId, String nickname, Integer gender, LocalDate birthDate, String description, Integer major, Integer school, String email, Integer systemLanguage, Integer languageMain, Integer languageLearn, Integer languageMainLevel, Integer languageLearnLevel, String profileImageUrl, Integer majorVisibility, Integer meetingVisibility, Integer likeVisibility, Integer guestbooksVisibility) {
        this.oauthId = oauthId;
        this.nickname = nickname;
        this.gender = gender;
        this.birthDate = birthDate;
        this.description = description;
        this.major = major;
        this.school = (school == null) ? 272 : school;
        this.email = email;
        this.systemLanguage = (systemLanguage == null) ? 1 : systemLanguage;
        this.languageMain = (languageMain == null) ? 1 : languageMain;
        this.languageLearn = (languageLearn == null) ? 2 : languageLearn;
        this.languageMainLevel = (languageMainLevel == null) ? 3 : languageMainLevel;
        this.languageLearnLevel = (languageLearnLevel == null) ? 3 : languageLearnLevel;
        this.profileImageUrl = profileImageUrl;
        this.majorVisibility = (majorVisibility == null) ? 1 : majorVisibility;
        this.meetingVisibility = (meetingVisibility == null) ? 1 : meetingVisibility;
        this.likeVisibility = (likeVisibility == null) ? 1 : likeVisibility;
        this.guestbooksVisibility = (guestbooksVisibility == null) ? 1 : guestbooksVisibility;
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

}
