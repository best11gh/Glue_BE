package org.glue.glue_be.chat.controller;

import lombok.RequiredArgsConstructor;
import org.glue.glue_be.chat.dto.request.GroupMessageReadRequest;
import org.glue.glue_be.chat.service.GroupChatService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller  // 웹소켓 메시지 처리를 위한 컨트롤러
@RequiredArgsConstructor
public class GroupChatWebSocketController {

    private final GroupChatService groupChatService;

    @MessageMapping("/group/{groupChatRoomId}/read-message")
    public void readGroupMessage(@DestinationVariable Long groupChatRoomId, @Payload GroupMessageReadRequest request) {
        groupChatService.markMessagesAsRead(groupChatRoomId, request.receiverId());
    }
}
