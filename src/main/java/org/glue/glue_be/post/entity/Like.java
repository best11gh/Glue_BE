package org.glue.glue_be.post.entity;

import lombok.*;

import jakarta.persistence.*;
import org.glue.glue_be.common.BaseEntity;
import org.glue.glue_be.user.entity.User;

@Entity
@Table(name = "like")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Builder
    private Like(User user, Post post) {
        this.user = user;
        this.post = post;
    }
}