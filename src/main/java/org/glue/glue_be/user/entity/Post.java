package org.glue.glue_be.user.entity;

import lombok.*;

import jakarta.persistence.*;
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

    @Column(name = "bumped_at", nullable = false)
    private LocalDateTime bumpedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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
        this.bumpedAt = bumpedAt != null ? bumpedAt : LocalDateTime.now();
    }

    // TODO: 컨벤션 확정짓기 (확정 후 주석 제거 예정)
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.viewCount == null) {
            this.viewCount = 0;
        }
    }

    // TODO: 컨벤션 확정짓기 (확정 후 주석 제거 예정)
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static Post createPost(Meeting meeting, String title, String content) {
        return Post.builder()
                .meeting(meeting)
                .title(title)
                .content(content)
                .build();
    }

    public void updatePost(String title, String content) {
        this.title = title;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void bump() {
        this.bumpedAt = LocalDateTime.now();
    }
}