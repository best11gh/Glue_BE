package org.glue.glue_be.post.entity;

import lombok.*;

import jakarta.persistence.*;
import org.glue.glue_be.meeting.entity.Meeting;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Column(name = "post_title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Column(name = "bumped_at", nullable = true)
    private LocalDateTime bumpedAt;

    @OneToMany(mappedBy = "post")
    private List<PostImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<Like> likes = new ArrayList<>();

    @Builder
    private Post(Meeting meeting, String title, String content, LocalDateTime bumpedAt) {
        this.meeting = meeting;
        this.title = title;
        this.content = content;
        this.viewCount = 0;
        this.bumpedAt = null;
    }

    public void updatePost(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void bumpPost() {
        this.bumpedAt = bumpedAt != null ? bumpedAt : LocalDateTime.now();
    }

    public void increaseViewCount() {
        this.viewCount++;
    }


}