package org.glue.glue_be.chat.controller;

import lombok.RequiredArgsConstructor;
import org.glue.glue_be.chat.dto.request.DmMessageReadRequest;
import org.glue.glue_be.chat.service.DmChatService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller  // 웹소켓 메시지 처리를 위한 컨트롤러
@RequiredArgsConstructor
public class DmChatWebSocketController {

    private final DmChatService dmChatService;

    @MessageMapping("/dm/{dmChatRoomId}/read-message")
    public void readDmMessage(@DestinationVariable Long dmChatRoomId, @Payload DmMessageReadRequest request) {
        dmChatService.markMessagesAsRead(dmChatRoomId, request.getReceiverId());
    }
}