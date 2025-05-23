package org.glue.glue_be.invitation.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.glue.glue_be.invitation.entity.Invitation;

import java.time.LocalDateTime;

public class InvitationDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {

        @NotNull(message = "미팅 ID는 필수입니다.")
        private Long meetingId;

        @Min(value = 1, message = "최대 사용 횟수는 1 이상이어야 합니다.")
        private Integer maxUses;

        @Min(value = 1, message = "만료 시간은 1시간 이상이어야 합니다.")
        private Integer expirationHours;

        private Long inviteeId;

    }

    @Getter
    @NoArgsConstructor
    public static class Response {
        private Long invitationId;
        private String code;
        private LocalDateTime expiresAt;
        private Integer maxUses;
        private Integer usedCount;
        private Integer status;
        private Long meetingId;
        private Long inviteeId;

        public static Response from(Invitation invitation) {
            Response response = new Response();
            response.invitationId = invitation.getInvitationId();
            response.code = invitation.getCode();
            response.expiresAt = invitation.getExpiresAt();
            response.maxUses = invitation.getMaxUses();
            response.usedCount = invitation.getUsedCount();
            response.status = invitation.getStatus();
            response.meetingId = invitation.getMeeting() != null ? invitation.getMeeting().getMeetingId() : null;
            response.inviteeId = invitation.getInviteeId();
            return response;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class AcceptRequest {
        @NotBlank(message = "초대장 코드는 필수 입력값입니다.")
        private String code;
    }
} 