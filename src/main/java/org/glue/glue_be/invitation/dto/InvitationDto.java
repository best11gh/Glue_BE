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

    @Getter
    @NoArgsConstructor
    public static class AcceptResponse {
        private Long meetingId;
        private String message;

        public AcceptResponse(Long meetingId, String message) {
            this.meetingId = meetingId;
            this.message = message;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class StatusResponse {
        private Long invitationId;
        private String code;
        private LocalDateTime expiresAt;
        private Integer maxUses;
        private Integer usedCount;
        private String status;
        private String statusDescription;
        private Boolean isValid;
        private Long meetingId;
        private String meetingTitle;
        private Long inviteeId;
        private LocalDateTime createdAt;

        public static StatusResponse from(Invitation invitation) {
            StatusResponse response = new StatusResponse();
            response.invitationId = invitation.getInvitationId();
            response.code = invitation.getCode();
            response.expiresAt = invitation.getExpiresAt();
            response.maxUses = invitation.getMaxUses();
            response.usedCount = invitation.getUsedCount();
            response.status = getStatusString(invitation.getStatus());
            response.statusDescription = getStatusDescription(invitation);
            response.isValid = invitation.isValid();
            response.meetingId = invitation.getMeeting() != null ? invitation.getMeeting().getMeetingId() : null;
            response.meetingTitle = invitation.getMeeting() != null ? invitation.getMeeting().getMeetingTitle() : null;
            response.inviteeId = invitation.getInviteeId();
            response.createdAt = invitation.getCreatedAt();
            return response;
        }

        private static String getStatusString(Integer status) {
            return switch (status) {
                case 1 -> "ACTIVE";
                case 2 -> "EXPIRED";
                case 3 -> "FULLY_USED";
                default -> "UNKNOWN";
            };
        }

        private static String getStatusDescription(Invitation invitation) {
            if (!invitation.isValid()) {
                if (LocalDateTime.now().isAfter(invitation.getExpiresAt())) {
                    return "초대장이 만료되었습니다";
                } else if (invitation.getUsedCount() >= invitation.getMaxUses()) {
                    return "초대장 사용 횟수가 초과되었습니다";
                } else {
                    return "초대장이 비활성화 상태입니다";
                }
            }
            return "초대장이 사용 가능한 상태입니다";
        }
    }
} 