package org.glue.glue_be.post.entity;

import lombok.*;

import jakarta.persistence.*;
import org.glue.glue_be.common.config.LocalDateTimeStringConverter;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.meeting.entity.Meeting;
import org.glue.glue_be.post.response.PostResponseStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Column(name = "post_title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Column(name = "bumped_at", nullable = true)
    @Convert(converter = LocalDateTimeStringConverter.class)
    private LocalDateTime bumpedAt;

    public static final int BUMP_LIMIT = 3;

    @Column(name = "bump_count", nullable = false)
    private Integer bumpCount;

    @OneToMany(mappedBy = "post")
    private List<PostImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<Like> likes = new ArrayList<>();

    @Builder
    private Post(Meeting meeting, String title, String content) {
        this.meeting = meeting;
        this.title = title;
        this.content = content;
        this.viewCount = 0;
        this.bumpedAt = null;
        this.bumpCount = 0;
    }

    public void updatePost(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void bump(LocalDateTime now) {
        if (bumpedAt != null && this.bumpCount >= BUMP_LIMIT) {
            throw new BaseException(PostResponseStatus.POST_CANNOT_BUMP_MORE);
        }
        this.bumpedAt = now;
        this.bumpCount++;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }


}