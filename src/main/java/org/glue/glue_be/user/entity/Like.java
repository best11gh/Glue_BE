package org.glue.glue_be.user.entity;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name = "like")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Builder
    private Like(User user, Post post) {
        this.user = user;
        this.post = post;
    }

    public static Like createLike(User user, Post post) {
        return Like.builder()
                .user(user)
                .post(post)
                .build();
    }
}