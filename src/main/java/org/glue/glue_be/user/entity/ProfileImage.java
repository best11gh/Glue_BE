package org.glue.glue_be.user.entity;

import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import lombok.*;
import org.glue.glue_be.common.BaseEntity;

@Entity
@Table(name = "profile_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_image_id")
    private Long profileImageId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Builder
    public ProfileImage(User user, String profileImageUrl) {
        this.user = user;
        this.profileImageUrl = profileImageUrl;
    }

    public void changeProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
