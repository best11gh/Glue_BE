package org.glue.glue_be.user.entity;

import jakarta.persistence.*;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "profile_image")
public class ProfileImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_image_id")
    private Long profileImageId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    protected ProfileImage() {
    }

    public ProfileImage(User user, String profileImageUrl) {
        this.user = user;
        this.profileImageUrl = profileImageUrl;
    }
}
