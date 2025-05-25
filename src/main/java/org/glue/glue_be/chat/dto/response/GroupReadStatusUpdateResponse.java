package org.glue.glue_be.chat.dto.response;

import java.util.List;

public record GroupReadStatusUpdateResponse(
        Long groupChatroomId,
        Long receiverId,
        List<GroupMessageResponse> readMessages
) {}