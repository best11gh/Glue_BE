package org.glue.glue_be.user.entity;

import jakarta.persistence.*;
import java.time.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @NotBlank
    @Size(max = 36)
    @Column(name = "uuid", nullable = false, unique = true, length = 36)
    private String uuid;

    @NotBlank
    @Column(name = "oauth_id", nullable = false, unique = true)
    private String oauthId;

    @NotBlank
    @Size(max = 20)
    @Column(name = "user_name", nullable = false, length = 20, unique = true)
    private String userName;

    @NotBlank
    @Size(max = 10)
    @Column(name = "nickname", nullable = false, length = 10, unique = true)
    private String nickname;

    @NotNull
    @Column(name = "gender", nullable = false)
    private Integer gender;

    @NotNull
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    // 언어로 할 거면 변경 필요
    @NotNull
    @Column(name = "nation", nullable = false)
    private String nation;

    @Column(name = "description", length = 50)
    @Size(max = 50)
    private String description;

    @NotNull
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
}
