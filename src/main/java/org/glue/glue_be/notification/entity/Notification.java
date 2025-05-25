package org.glue.glue_be.notification.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.*;
import org.glue.glue_be.common.BaseEntity;
import org.glue.glue_be.user.entity.User;


@Entity
@Table(name = "notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Notification extends BaseEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    // 알림을 받은 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    // guestbook, post, notice 중 하나
    private String type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    // type이 guestbook인 경우에만 존재
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guestbook_host_id")
    private User guestbookHost;

    @Builder
    private Notification(User receiver, String type, String title, String content, Long targetId, User guestbookHost) {
        this.receiver = receiver;
        this.type = type;
        this.title = title;
        this.content = content;
        this.targetId = targetId;
        this.guestbookHost = guestbookHost;
    }


}
