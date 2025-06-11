package org.glue.glue_be.util.fcm.dto;

import lombok.*;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmSendDto {
    private String token;
    private String title;
    private String body;
    private String type;           // 알림 타입 guestbook, post, notice, dm, group 중 하나
    private Long id;               // targetId
    private Long guestbookHostId;  // guestbook 타입일 때만 사용

    @Builder(toBuilder = true)
    public FcmSendDto(String token, String title, String body, String type, Long id, Long guestbookHostId) {
        this.token = token;
        this.title = title;
        this.body = body;
        this.type = type;
        this.id = id;
        this.guestbookHostId = guestbookHostId;
    }
}