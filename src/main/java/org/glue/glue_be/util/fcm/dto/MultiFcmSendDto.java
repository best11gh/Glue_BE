package org.glue.glue_be.util.fcm.dto;

import lombok.*;
import java.util.List;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MultiFcmSendDto {
    private List<String> tokens;
    private String title;
    private String body;
    private String type;
    private Long id;
    private Long guestbookHostId;  // guestbook 타입일 때만 사용

    @Builder(toBuilder = true)
    public MultiFcmSendDto(List<String> tokens, String title, String body, String type, Long id, Long guestbookHostId) {
        this.tokens = tokens;
        this.title = title;
        this.body = body;
        this.type = type;
        this.id = id;
        this.guestbookHostId = guestbookHostId;
    }
}