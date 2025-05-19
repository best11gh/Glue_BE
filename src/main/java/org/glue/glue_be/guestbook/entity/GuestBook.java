package org.glue.glue_be.guestbook.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.*;
import org.glue.glue_be.common.BaseEntity;
import org.glue.glue_be.guestbook.dto.request.UpdateGuestBookRequest;
import org.glue.glue_be.user.entity.User;

@Entity
@Table(name = "guest_book")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class GuestBook extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guest_book_id")
    private Long guestBookId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private User writer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private GuestBook parent;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "secret", nullable = false)
    private boolean secret;

    @Builder
    private GuestBook(User host,
                      User writer,
                      GuestBook parent,
                      String content,
                      boolean secret) {
        this.host = host;
        this.writer = writer;
        this.parent = parent;
        this.content = content;
        this.secret = secret;
    }

    public void update(UpdateGuestBookRequest request){
        this.content = request.content();
        this.secret = request.secret();
    }


}
