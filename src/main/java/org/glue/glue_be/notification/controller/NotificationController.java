package org.glue.glue_be.notification.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.glue.glue_be.auth.jwt.CustomUserDetails;
import org.glue.glue_be.common.response.BaseResponse;
import org.glue.glue_be.notification.dto.request.CreateNotificationRequest;
import org.glue.glue_be.notification.dto.response.NotificationResponse;
import org.glue.glue_be.notification.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification")
@Tag(name = "Notification", description = "알림 관련 API")
public class NotificationController {

    private final NotificationService notificationService;

    // TODO: 테스트 용도, 개발 완료 후 삭제 예정
    @PostMapping
    @Operation(summary = "알림 생성 (테스트용)")
    public BaseResponse<Void> create(@Valid @RequestBody CreateNotificationRequest request) {
        notificationService.create(request);
        return new BaseResponse<>();
    }

    @GetMapping
    @Operation(summary = "알림 목록 조회", description = "사용자의 일반/공지 알림 목록을 조회")
    public BaseResponse<NotificationResponse[]> getNotifications(
            @AuthenticationPrincipal CustomUserDetails auth,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "false") boolean isNoticeTab
    ) {
        NotificationResponse[] notifications = notificationService.getNotifications(
                auth.getUserId(), cursorId, pageSize, isNoticeTab
        );
        return new BaseResponse<>(notifications);
    }


    @DeleteMapping("/{notificationId}")
    @Operation(summary = "알림 삭제")
    public BaseResponse<Void> delete(@PathVariable Long notificationId,
                                     @AuthenticationPrincipal CustomUserDetails auth) {
        notificationService.delete(notificationId, auth.getUserId());
        return new BaseResponse<>();
    }

}
