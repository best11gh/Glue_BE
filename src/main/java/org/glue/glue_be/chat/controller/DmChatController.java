package org.glue.glue_be.chat.controller;

import lombok.RequiredArgsConstructor;
import org.glue.glue_be.auth.jwt.CustomUserDetails;
import org.glue.glue_be.chat.dto.request.DmChatRoomCreateRequest;
import org.glue.glue_be.chat.dto.request.DmChatRoomJoinRequest;
import org.glue.glue_be.chat.dto.request.DmMessageReadRequest;
import org.glue.glue_be.chat.dto.request.DmMessageSendRequest;
import org.glue.glue_be.chat.dto.response.*;
import org.glue.glue_be.chat.service.DmChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/dm")
@RequiredArgsConstructor
public class DmChatController {

    private final DmChatService dmChatService;
    private final SimpMessagingTemplate messagingTemplate;
    private static final UUID TEST_USER_UUID = UUID.fromString("b2d6f7c9-a842-41e5-b390-63487e10d9fc");
    private static final UUID TEST_USER_UUID_2 = UUID.fromString("c3e7f8d0-b953-42f6-c4a1-74598f21e0ad");
    private static final UUID TEST_USER_UUID_3 = UUID.fromString("d4f8f9e1-c064-53a7-d5b2-85609a32f1be");

    // Dm 채팅방 생성
    @PostMapping("/rooms/create")
    public ResponseEntity<DmChatRoomCreateResult> createDmChatRoom(@RequestBody DmChatRoomCreateRequest request, @AuthenticationPrincipal CustomUserDetails auth) {
        // TODO: 테스트용이므로 제거
        UUID userUuid = TEST_USER_UUID;
        DmChatRoomCreateResult result = dmChatService.createDmChatRoom(request, userUuid);

//        DmChatRoomCreateResult result = dmChatService.createDmChatRoom(request, auth.getUserUuid());
        return ResponseEntity.status(result.getStatus().getCode()).body(result);
    }

    // 채팅방 상세 정보 (채팅방 오른쪽 토글: 알림 정보, 초대 여부, 참여자 정보 확인 가능)
    @GetMapping("/rooms/{dmChatRoomId}")
    public ResponseEntity<DmChatRoomDetailResponse> getDmChatRoomDetail(@PathVariable Long dmChatRoomId, @AuthenticationPrincipal CustomUserDetails auth) {
        // TODO: 테스트용이므로 제거
        UUID userUuid = TEST_USER_UUID;
        DmChatRoomDetailResponse response = dmChatService.getDmChatRoomDetail(dmChatRoomId, Optional.ofNullable(userUuid));

//        DmChatRoomDetailResponse response = dmChatService.getDmChatRoomDetail(dmChatRoomId, Optional.ofNullable(auth.getUserUuid()));
        return ResponseEntity.ok(response);
    }

    // 내가 호스트인 DM 채팅방 목록 조회
    @GetMapping("/rooms/hosted")
    public ResponseEntity<List<DmChatRoomListResponse>> getHostedDmChatRooms(
            @AuthenticationPrincipal CustomUserDetails auth) {
        // TODO: 테스트용이므로 제거
        UUID userUuid = TEST_USER_UUID_2;
        List<DmChatRoomListResponse> chatRooms = dmChatService.getHostedDmChatRooms(userUuid);

//        List<DmChatRoomListResponse> chatRooms = dmChatService.getHostedDmChatRooms(auth.getUserUuid());
        return ResponseEntity.ok(chatRooms);
    }

    // 내가 참석자인 DM 채팅방 목록 조회
    @GetMapping("/rooms/participated")
    public ResponseEntity<List<DmChatRoomListResponse>> getParticipatedDmChatRooms(
            @AuthenticationPrincipal CustomUserDetails auth) {
        // TODO: 테스트용이므로 제거
        UUID userUuid = TEST_USER_UUID_3;
        List<DmChatRoomListResponse> chatRooms = dmChatService.getParticipatedDmChatRooms(userUuid);

//        List<DmChatRoomListResponse> chatRooms = dmChatService.getParticipatedDmChatRooms(auth.getUserUuid());
        return ResponseEntity.ok(chatRooms);
    }

    // Dm방 나가기
    @DeleteMapping("/rooms/{dmChatRoomId}/leave")
    public ResponseEntity<List<DmActionResponse>> leaveChatRoom(@PathVariable Long dmChatRoomId, @AuthenticationPrincipal CustomUserDetails auth
    ) {
        // TODO: 테스트용이므로 제거
        UUID userUuid = TEST_USER_UUID_2;
        List<DmActionResponse> response = dmChatService.leaveDmChatRoom(dmChatRoomId, userUuid);

//        List<DmActionResponse> response = dmChatService.leaveDmChatRoom(dmChatRoomId, auth.getUserUuid());
        return ResponseEntity.ok(response);
    }

    // Dm방 클릭 시, 대화 이력을 불러오면서 + 읽지 않은 메시지들 읽음으로 처리
    @PutMapping("/{dmChatRoomId}/all-messages")
    public ResponseEntity<List<DmMessageResponse>> getDmMessages(@PathVariable Long dmChatRoomId, @AuthenticationPrincipal CustomUserDetails auth) {
        // TODO: 테스트용이므로 제거
        UUID userUuid = TEST_USER_UUID;
        List<DmMessageResponse> messages = dmChatService.getDmMessagesByDmChatRoomId(dmChatRoomId, userUuid);


//        List<DmMessageResponse> messages = dmChatService.getDmMessagesByDmChatRoomId(dmChatRoomId, auth.getUserUuid());
        return ResponseEntity.ok(messages);
    }

    // 메시지 전송
    @PostMapping("/{dmChatRoomId}/send-message")
    public ResponseEntity<DmMessageResponse> sendMessage(
            @PathVariable Long dmChatRoomId,
            @RequestBody DmMessageSendRequest request,
            @AuthenticationPrincipal CustomUserDetails auth) {
        // TODO: 테스트용이므로 제거
//        UUID userUuid = TEST_USER_UUID;
//        DmMessageResponse response = dmChatService.processDmMessage(dmChatRoomId, request, auth.getUserUuid());

        DmMessageResponse response = dmChatService.processDmMessage(dmChatRoomId, request, auth.getUserUuid());
        return ResponseEntity.ok(response);
    }

    // Websocket: Dm창 동시 접속 시 곧바로 읽음 처리
    // @RequestMapping("/api/dm")과 @MessageMapping는 독립적으로 작동하기 때문에 /dm을 별도로 붙여줌
    @MessageMapping("/dm/{dmChatRoomId}/readMessage")
    public void readDmMessage(@DestinationVariable Long dmChatRoomId, @Payload DmMessageReadRequest request) {
        // 읽음 상태 처리
        dmChatService.markMessagesAsRead(dmChatRoomId, request.getReceiverId());
    }
}