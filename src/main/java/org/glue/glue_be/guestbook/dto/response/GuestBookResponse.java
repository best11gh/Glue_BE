package org.glue.glue_be.guestbook.dto.response;


import java.time.LocalDateTime;
import lombok.*;
import org.glue.glue_be.common.dto.UserSummary;
import org.glue.glue_be.guestbook.entity.GuestBook;

@Getter
@Builder
public class GuestBookResponse {
    private Long id;

    private UserSummary writer;

    private String content;

    private boolean canShow;

    private LocalDateTime createdAt;


    public static GuestBookResponse fromEntity(GuestBook guestBook, boolean canShow) {
        UserSummary writer = UserSummary.builder()
                .userId(guestBook.getWriter().getUserId())
                .userNickname(guestBook.getWriter().getNickname())
                .profileImageUrl(guestBook.getWriter().getProfileImageUrl())
                .build();

        return GuestBookResponse.builder()
                .id(guestBook.getGuestBookId())
                .writer(writer)
                .content(guestBook.getContent())
                .canShow(canShow)
                .createdAt(guestBook.getCreatedAt())
                .build();
    }

}
