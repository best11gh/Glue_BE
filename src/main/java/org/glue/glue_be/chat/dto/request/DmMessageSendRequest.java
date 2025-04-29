package org.glue.glue_be.chat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DmMessageSendRequest {
    private Long senderId;
    private String content;
}