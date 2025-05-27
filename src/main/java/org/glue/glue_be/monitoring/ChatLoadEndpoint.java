package org.glue.glue_be.monitoring;

import lombok.RequiredArgsConstructor;
import org.glue.glue_be.chat.dto.request.DmMessageSendRequest;
import org.glue.glue_be.chat.dto.response.DmMessageResponse;
import org.glue.glue_be.chat.service.DmChatService;
import org.springframework.stereotype.Component;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

import java.util.stream.IntStream;

@Component
@Endpoint(id = "chatload")
@RequiredArgsConstructor
public class ChatLoadEndpoint {

    private final DmChatService dmChatService;

    /**
     * @param roomId 부하를 걸 채팅방 ID
     * @param count  생성할 메시지 수
     */
    @WriteOperation
    public String generateLoad(Long roomId, int count) {
        IntStream.range(0, count).parallel().forEach(i -> {
            // 여기서 processDmMessage는 실제 메시지 발송·저장 로직을 갖고 있어야 합니다.
            DmMessageResponse resp = dmChatService.processDmMessage(
                    roomId,
                    new DmMessageSendRequest("auto message " + i),
                    0L  // 테스트용 유저 ID
            );
        });
        return "Generated " + count + " messages in room " + roomId;
    }
}
