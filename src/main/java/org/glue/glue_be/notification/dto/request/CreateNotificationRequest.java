package org.glue.glue_be.notification.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateNotificationRequest {

        @NotNull(message = "수신자 ID는 필수입니다.")
        private Long receiverId;

        @NotBlank(message = "알림 유형은 필수입니다.")
        @Pattern(regexp = "guestbook|post", message = "알림 유형은 'guestbook' 또는 'post'여야 합니다.")
        private String type;

        @NotBlank(message = "알림 제목은 필수입니다.")
        private String title;

        @NotBlank(message = "알림 내용은 필수입니다.")
        private String content;

        @NotNull(message = "대상 ID는 필수입니다.")
        private Long targetId;

        private Long guestbookHostId;

        @Builder
        public CreateNotificationRequest(Long receiverId, String type, String title, String content, Long targetId,
                                         Long guestbookHostId) {
                this.receiverId = receiverId;
                this.type = type;
                this.title = title;
                this.content = content;
                this.targetId = targetId;
                this.guestbookHostId = guestbookHostId;
        }
}
