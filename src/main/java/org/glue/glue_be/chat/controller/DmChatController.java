package org.glue.glue_be.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.glue.glue_be.auth.jwt.CustomUserDetails;
import org.glue.glue_be.chat.dto.request.DmChatRoomCreateRequest;
import org.glue.glue_be.chat.dto.request.DmMessageSendRequest;
import org.glue.glue_be.chat.dto.response.*;
import org.glue.glue_be.chat.service.DmChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/dm")
@RequiredArgsConstructor
@Tag(name = "Dm Chat", description = "쪽지 API")
public class DmChatController {

    private final DmChatService dmChatService;
//    private final MeterRegistry registry;

    // Dm 채팅방 생성
    @PostMapping("/rooms/create")
    @Operation(summary = "채팅방 생성")
    public ResponseEntity<DmChatRoomCreateResult> createDmChatRoom(@RequestBody DmChatRoomCreateRequest request, @AuthenticationPrincipal CustomUserDetails auth) {
        DmChatRoomCreateResult result = dmChatService.createDmChatRoom(request, auth.getUserId());
        return ResponseEntity.status(result.getStatus().code()).body(result);
    }

    // 채팅방 상세 정보 (채팅방 오른쪽 토글: 알림 정보, 초대 여부, 참여자 정보 확인 가능)
    @GetMapping("/rooms/{dmChatRoomId}")
    @Operation(summary = "채팅방 상세 정보")
    public ResponseEntity<DmChatRoomDetailResponse> getDmChatRoomDetail(@PathVariable Long dmChatRoomId, @AuthenticationPrincipal CustomUserDetails auth) {
        DmChatRoomDetailResponse response = dmChatService.getDmChatRoomDetail(dmChatRoomId, Optional.ofNullable(auth.getUserId()));
        return ResponseEntity.ok(response);
    }

    // 채팅방 알림 상태 토글
    @PutMapping("/{dmChatRoomId}/toggle-push-notification")
    @Operation(summary = "채팅방 알림 상태")
    public Integer togglePushNotification(
            @PathVariable Long dmChatRoomId, @AuthenticationPrincipal CustomUserDetails auth) {
        return dmChatService.toggleDmPushNotification(dmChatRoomId, 1L);
    }

    // 내가 호스트인 DM 채팅방 목록 조회
    @GetMapping("/rooms/hosted")
    @Operation(summary = "내가 호스트인 DM 채팅방 목록 조회")
    public ResponseEntity<List<DmChatRoomListResponse>> getHostedDmChatRooms(
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @AuthenticationPrincipal CustomUserDetails auth) {
        List<DmChatRoomListResponse> chatRooms = dmChatService.getHostedDmChatRooms(cursorId, pageSize, auth.getUserId());
        return ResponseEntity.ok(chatRooms);
    }

    // 내가 참석자인 DM 채팅방 목록 조회
    @GetMapping("/rooms/participated")
    @Operation(summary = "내가 참석자인 DM 채팅방 목록 조회")
    public ResponseEntity<List<DmChatRoomListResponse>> getParticipatedDmChatRooms(
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @AuthenticationPrincipal CustomUserDetails auth) {
        List<DmChatRoomListResponse> chatRooms = dmChatService.getParticipatedDmChatRooms(cursorId, pageSize, auth.getUserId());
        return ResponseEntity.ok(chatRooms);
    }

    // Dm방 나가기
    @DeleteMapping("/rooms/{dmChatRoomId}/leave")
    @Operation(summary = "DM방 나가기")
    public ResponseEntity<List<ActionResponse>> leaveChatRoom(@PathVariable Long dmChatRoomId, @AuthenticationPrincipal CustomUserDetails auth
    ) {
        List<ActionResponse> response = dmChatService.leaveDmChatRoom(dmChatRoomId, auth.getUserId());
        return ResponseEntity.ok(response);
    }

    // Dm방 클릭 시, 대화 이력을 불러오면서 + 읽지 않은 메시지들 읽음으로 처리
    @PutMapping("/{dmChatRoomId}/all-messages")
    @Operation(summary = "DM방 대화 이력 조회 (읽음 처리 포함)")
    public ResponseEntity<List<DmMessageResponse>> getDmMessages(
            @PathVariable Long dmChatRoomId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @AuthenticationPrincipal CustomUserDetails auth) {
        List<DmMessageResponse> messages = dmChatService.getDmMessagesByDmChatRoomId(dmChatRoomId, cursorId, pageSize, auth.getUserId());
        return ResponseEntity.ok(messages);
    }

    // 메시지 전송
    @PostMapping("/{dmChatRoomId}/send-message")
    @Operation(summary = "메시지 전송")
    public ResponseEntity<DmMessageResponse> sendMessage(
            @PathVariable Long dmChatRoomId,
            @RequestBody DmMessageSendRequest request,
            @AuthenticationPrincipal CustomUserDetails auth) {

//        // 메시지 전송 API 호출 전 카운터 증가
//        registry.counter("dm.messages.sent", "room", dmChatRoomId.toString())
//                .increment();
//
//        DmMessageResponse response = dmChatService.processDmMessage(
//                dmChatRoomId, request, auth.getUserId());
        DmMessageResponse response = dmChatService.processDmMessage(dmChatRoomId, request, auth.getUserId());
        return ResponseEntity.ok(response);
    }
}