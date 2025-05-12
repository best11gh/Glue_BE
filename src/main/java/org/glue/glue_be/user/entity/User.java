package org.glue.glue_be.user.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;

import lombok.*;
import org.glue.glue_be.common.BaseEntity;
import org.glue.glue_be.common.config.LocalDateStringConverter;
import org.glue.glue_be.common.config.UUIDStringConverter;

@Getter
@Entity
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_uuid", nullable = false, unique = true, length = 36)
    @Convert(converter = UUIDStringConverter.class)
    private UUID uuid;

    @Column(name = "oauth_id", nullable = false, unique = true)
    private String oauthId;

    @Column(name = "user_name", nullable = false, length = 20)
    private String userName;

    @Column(name = "nickname", nullable = false, length = 10, unique = true)
    private String nickname;

    @Column(name = "gender", nullable = false)
    private Integer gender;

    @Column(name = "birth_date", nullable = false)
    @Convert(converter = LocalDateStringConverter.class)
    private LocalDate birthDate;

    // TODO: 언어로 할 거면 변경 필요 (+ 함수도)
    @Column(name = "nation", nullable = false)
    private Integer nation;

    @Column(name = "description", length = 50)
    private String description;

    @Column(name = "certified", nullable = false)
    private Integer certified = 0;

    @Column(name = "major", nullable = false)
    private Integer major;

    @Column(name = "major_visibility", nullable = false)
    private Integer majorVisibility;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private ProfileImage profileImage;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Builder
    public User(UUID uuid, String oauthId, String userName, String nickname, Integer gender, LocalDate birth,
                Integer nation, String description, Integer certified, Integer major, Integer majorVisibility
    ) {
        this.uuid = uuid;
        this.oauthId = oauthId;
        this.userName = userName;
        this.nickname = nickname;
        this.gender = gender;
        this.birthDate = birth;
        this.nation = nation;
        this.description = description;
        this.certified = certified;
        this.major = major;
        this.majorVisibility = majorVisibility;
        this.fcmToken = null;
    }

    public void changeName(String name) {
        this.userName = name;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changeNation(Integer nation) {
        this.nation = nation;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    public void changeCertified(int certified) {
        this.certified = certified;
    }

    public void changeFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

}
