package org.glue.glue_be.chat.controller;

import lombok.RequiredArgsConstructor;
import org.glue.glue_be.chat.dto.request.DmChatRoomCreateRequest;
import org.glue.glue_be.chat.dto.request.DmChatRoomJoinRequest;
import org.glue.glue_be.chat.dto.request.DmMessageReadRequest;
import org.glue.glue_be.chat.dto.request.DmMessageSendRequest;
import org.glue.glue_be.chat.dto.response.*;
import org.glue.glue_be.chat.entity.dm.DmChatRoom;
import org.glue.glue_be.chat.service.DmChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dm")
@RequiredArgsConstructor
public class DmChatController {

    private final DmChatService dmChatService;
    private final SimpMessagingTemplate messagingTemplate;

    // Dm 채팅방 생성
    @PostMapping("/create")
    public ResponseEntity<DmChatRoomCreateResult> createDmChatRoom(@RequestBody DmChatRoomCreateRequest request) {
        DmChatRoomCreateResult result = dmChatService.createDmChatRoom(request);
        return ResponseEntity.status(result.getStatus().getCode()).body(result);
    }

    // Dm 채팅방 상세 정보 조회
    // TODO: 필요한지 좀 더 생각해보기
    @GetMapping("/rooms/{dmChatRoomId}")
    public ResponseEntity<DmChatRoomDetailResponse> getDmChatRoomDetail(@PathVariable Long dmChatRoomId) {
        DmChatRoomDetailResponse response = dmChatService.getDmChatRoomDetail(dmChatRoomId);
        return ResponseEntity.ok(response);
    }

    // 내가 호스트인 DM 채팅방 목록 조회
    @GetMapping("/rooms/hosted")
    public ResponseEntity<List<DmChatRoomListResponse>> getHostedDmChatRooms(
            @RequestParam Long userId) {
        List<DmChatRoomListResponse> chatRooms = dmChatService.getHostedDmChatRooms(userId);
        return ResponseEntity.ok(chatRooms);
    }

    // 내가 참석자인 DM 채팅방 목록 조회
    @GetMapping("/rooms/participated")
    public ResponseEntity<List<DmChatRoomListResponse>> getParticipatedDmChatRooms(
            @RequestParam Long userId) {
        List<DmChatRoomListResponse> chatRooms = dmChatService.getParticipatedDmChatRooms(userId);
        return ResponseEntity.ok(chatRooms);
    }

    // Dm방 나가기
    @DeleteMapping("/{dmChatRoomId}/leave")
    public ResponseEntity<List<DmActionResponse>> leaveChatRoom(@PathVariable Long dmChatRoomId, @RequestParam Long userId
    ) {
        List<DmActionResponse> response = dmChatService.leaveDmChatRoom(dmChatRoomId, userId);
        return ResponseEntity.ok(response);
    }

    // Dm방 클릭 시, 대화 이력을 불러오면서 + 읽지 않은 메시지들 읽음으로 처리
    @PutMapping("/{dmChatRoomId}/all-messages")
    public ResponseEntity<List<DmMessageResponse>> getDmMessages(@PathVariable Long dmChatRoomId, @RequestParam Long userId) {
        List<DmMessageResponse> messages = dmChatService.getDmMessagesByDmChatRoomId(dmChatRoomId, userId);
        return ResponseEntity.ok(messages);
    }

    // Websocket: Dm 전송
    // @RequestMapping("/api/dm")과 @MessageMapping는 독립적으로 작동하기 때문에 /dm을 별도로 붙여줌
    @MessageMapping("/dm/{dmChatRoomId}/sendMessage")
    public void sendDmMessage(@DestinationVariable Long dmChatRoomId, @Payload DmMessageSendRequest request) {

        DmMessageResponse message = dmChatService.saveDmMessage(dmChatRoomId, request);

        // 1:1 채팅이므로 특정 사용자들에게만 메시지 전송
        // 수신자에게 (채팅방의 다른 참여자)
        DmChatRoomDetailResponse chatRoom = dmChatService.getDmChatRoomDetail(dmChatRoomId);
        for (DmChatRoomDetailResponse.ChatUserResponse participant : chatRoom.getParticipants()) {
            if (!participant.getUserId().equals(request.getSenderId())) {
                messagingTemplate.convertAndSend("/queue/dm/" + participant.getUserId(), message);
            }
        }
    }

    // Websocket: Dm창 동시 접속 시 곧바로 읽음 처리
    @MessageMapping("/dm/{dmChatRoomId}/readMessage")
    public void readDmMessage(@DestinationVariable Long dmChatRoomId, @Payload DmMessageReadRequest request) {
        // 읽음 상태 처리
        dmChatService.markMessagesAsRead(dmChatRoomId, request.getReceiverId());
    }
}