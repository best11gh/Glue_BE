package org.glue.glue_be.notice.entity;


import jakarta.persistence.*;
import java.util.*;
import lombok.*;
import org.glue.glue_be.common.BaseEntity;
import org.glue.glue_be.notice.dto.request.NoticeRequest;

@Entity
@Table(name = "notice")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long noticeId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @OneToMany(mappedBy = "notice")
    private List<NoticeImage> images = new ArrayList<>();

    @Builder
    public Notice(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void update(NoticeRequest request) {
        this.title = request.title();
        this.content = request.content();
    }

}
