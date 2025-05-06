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

    @Builder(toBuilder = true)
    public MultiFcmSendDto(List<String> tokens, String title, String body) {
        this.tokens = tokens;
        this.title = title;
        this.body = body;
    }
}
