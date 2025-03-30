package org.glue.glue_be.user.entity;

import jakarta.persistence.*;
import java.time.*;
import lombok.*;

@Entity
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "uuid", nullable = false, unique = true, length = 36)
    private String uuid;

    @Column(name = "oauth_id", nullable = false, unique = true)
    private String oauthId;

    @Column(name = "user_name", nullable = false, length = 20)
    private String userName;

    @Column(name = "nickname", nullable = false, length = 10, unique = true)
    private String nickname;

    @Column(name = "gender", nullable = false)
    private Integer gender;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    // TODO: 언어로 할 거면 변경 필요 (+ 함수도)
    @Column(name = "nation", nullable = false)
    private String nation;

    @Column(name = "description", length = 50)
    private String description;

    @Column(name = "certified", nullable = false)
    private Integer certified = 0;

    @Builder
    public User(String uuid, String oauthId, String userName, String nickname, Integer gender, LocalDate birth,
                String nation, String description, Integer certified) {
        this.uuid = uuid;
        this.oauthId = oauthId;
        this.userName = userName;
        this.nickname = nickname;
        this.gender = gender;
        this.birthDate = birth;
        this.nation = nation;
        this.description = description;
        this.certified = certified;
    }

    public void changeName(String name) {
        if (name == null || name.isBlank() || name.length() > 20) {
            throw new IllegalArgumentException("이름은 20자 이하의 빈 값이 아니어야 합니다.");
        }
        this.userName = name;
    }

    public void changeNickname(String nickname) {
        if (nickname == null || nickname.isBlank() || nickname.length() > 10) {
            throw new IllegalArgumentException("닉네임은 10자 이하의 빈 값이 아니어야 합니다.");
        }
        this.nickname = nickname;
    }

    public void changeNation(String nation) {
        if (nation == null || nation.isBlank()) {
            throw new IllegalArgumentException("국가는 빈 값이 아니어야 합니다.");
        }
        this.nation = nation;
    }

    public void changeDescription(String description) {
        if (description == null || description.isBlank() || description.length() > 50) {
            throw new IllegalArgumentException("한마디는 20자 이하의 빈 값이 아니어야 합니다.");
        }
        this.description = description;
    }

    public void changeCertified(int certified){
        if (certified != 0 && certified != 1){
            throw new IllegalArgumentException("인증 값은 0 또는 1이어야 합니다.");
        }
        this.certified = certified;
    }

}
