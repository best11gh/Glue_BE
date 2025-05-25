package org.glue.glue_be.notice.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.*;
import org.glue.glue_be.common.BaseEntity;
import org.glue.glue_be.notice.dto.response.NoticeImageResponse;

@Entity
@Table(name = "notice_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "image_order", nullable = false)
    private Integer imageOrder;

    @Builder
    private NoticeImage(Notice notice, String imageUrl, Integer imageOrder) {
        this.notice = notice;
        this.imageUrl = imageUrl;
        this.imageOrder = imageOrder;
    }

}
