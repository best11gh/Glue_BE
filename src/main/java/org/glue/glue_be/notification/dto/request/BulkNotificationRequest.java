package org.glue.glue_be.notification.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;
import lombok.*;

@Getter
@NoArgsConstructor
public class BulkNotificationRequest {

    @NotEmpty(message = "수신자 ID 리스트는 필수입니다.")
    private List<Long> receiverIds;

    @NotBlank(message = "알림 유형은 필수입니다.")
    @Pattern(regexp = "guestbook|post|notice", message = "알림 유형은 'guestbook', 'post', 'notice'여야 합니다.")
    private String type;

    @NotBlank(message = "알림 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "알림 내용은 필수입니다.")
    private String content;

    @NotNull(message = "대상 ID는 필수입니다.")
    private Long targetId;

    private Long guestbookHostId;

    @Builder
    public BulkNotificationRequest(List<Long> receiverIds, String type, String title, String content, Long targetId,
                                   Long guestbookHostId) {
        this.receiverIds = receiverIds;
        this.type = type;
        this.title = title;
        this.content = content;
        this.targetId = targetId;
        this.guestbookHostId = guestbookHostId;
    }
}