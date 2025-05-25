package org.glue.glue_be.notification.controller;


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
public class NotificationController {

    private final NotificationService notificationService;

    // TODO: 테스트 용도, 개발 완료 후 삭제 예정
    @PostMapping
    public BaseResponse<Void> create(@Valid @RequestBody CreateNotificationRequest request) {
        notificationService.create(request);
        return new BaseResponse<>();
    }

    @GetMapping
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
    public BaseResponse<Void> delete(@PathVariable Long notificationId,
                                     @AuthenticationPrincipal CustomUserDetails auth) {
        notificationService.delete(notificationId, auth.getUserId());
        return new BaseResponse<>();
    }

}
