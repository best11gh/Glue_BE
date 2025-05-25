package org.glue.glue_be.chat.controller;

import lombok.RequiredArgsConstructor;
import org.glue.glue_be.auth.jwt.CustomUserDetails;
import org.glue.glue_be.chat.dto.request.GroupMessageSendRequest;
import org.glue.glue_be.chat.dto.response.*;
import org.glue.glue_be.chat.service.GroupChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
public class GroupChatController {

    private final GroupChatService groupChatService;

    // 그룹 채팅방 생성
    @GetMapping("/rooms/create/{meetingId}")
    public ResponseEntity<GroupChatRoomCreateResult> createGroupChatRoom(@PathVariable Long meetingId, @AuthenticationPrincipal CustomUserDetails auth) {
        GroupChatRoomCreateResult result = groupChatService.createGroupChatRoom(meetingId, auth.getUserId());
        return ResponseEntity.status(result.status().code()).body(result);
    }

    // 채팅방 상세 정보 조회
    @GetMapping("/rooms/{groupChatroomId}")
    public ResponseEntity<GroupChatRoomDetailResponse> getGroupChatRoomDetail(@PathVariable Long groupChatroomId, @AuthenticationPrincipal CustomUserDetails auth) {
        GroupChatRoomDetailResponse response = groupChatService.getGroupChatRoomDetail(groupChatroomId, auth.getUserId());
        return ResponseEntity.ok(response);
    }

    // 채팅방 알림 상태 토글
    @PutMapping("/{groupChatroomId}/toggle-push-notification")
    public Integer togglePushNotification(@PathVariable Long groupChatroomId, @AuthenticationPrincipal CustomUserDetails auth) {
        return groupChatService.toggleGroupPushNotification(groupChatroomId, auth.getUserId());
    }

    // 내가 참여 중인 그룹 채팅방 목록 조회
    @GetMapping("/rooms/list")
    public ResponseEntity<List<GroupChatRoomListResponse>> getGroupChatRooms(@AuthenticationPrincipal CustomUserDetails auth) {
        List<GroupChatRoomListResponse> chatRooms = groupChatService.getGroupChatRooms(auth.getUserId());
        return ResponseEntity.ok(chatRooms);
    }

    // 그룹 채팅방 나가기
    @DeleteMapping("/rooms/{groupChatroomId}/leave")
    public ResponseEntity<List<ActionResponse>> leaveChatRoom(@PathVariable Long groupChatroomId, @AuthenticationPrincipal CustomUserDetails auth) {
        List<ActionResponse> response = groupChatService.leaveGroupChatRoom(groupChatroomId, auth.getUserId());
        return ResponseEntity.ok(response);
    }

    // 채팅방 클릭 시, 대화 이력을 불러오면서 + 읽지 않은 메시지들 읽음으로 처리
    @PutMapping("/{groupChatroomId}/all-messages")
    public ResponseEntity<List<GroupMessageResponse>> getGroupMessages(@PathVariable Long groupChatroomId, @AuthenticationPrincipal CustomUserDetails auth) {
        List<GroupMessageResponse> messages = groupChatService.getGroupMessagesByGroupChatRoomId(groupChatroomId, auth.getUserId());
        return ResponseEntity.ok(messages);
    }

    // 메시지 전송
    @PostMapping("/{groupChatroomId}/send-message")
    public ResponseEntity<GroupMessageResponse> sendMessage(
            @PathVariable Long groupChatroomId,
            @RequestBody GroupMessageSendRequest request,
            @AuthenticationPrincipal CustomUserDetails auth) {
        GroupMessageResponse response = groupChatService.processGroupMessage(groupChatroomId, request, auth.getUserId());
        return ResponseEntity.ok(response);
    }
}