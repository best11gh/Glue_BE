package org.glue.glue_be.invitation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.glue.glue_be.invitation.entity.Invitation;

import java.time.LocalDateTime;

public class InvitationDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private Integer maxUses;
        private Integer expirationHours;
        private Long meetingId;
        private Long inviteeId;
        
        public void setMeetingId(Long meetingId) {
            this.meetingId = meetingId;
        }
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
        private String code;
    }
} 