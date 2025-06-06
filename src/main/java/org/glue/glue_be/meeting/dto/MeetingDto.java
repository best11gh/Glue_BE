package org.glue.glue_be.meeting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.glue.glue_be.meeting.entity.Meeting;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class MeetingDto {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "모임 제목은 필수입니다")
        private String meetingTitle;

        @NotNull(message = "모임 시간은 필수입니다")
        @Future(message = "모임 시간은 현재 시간 이후여야 합니다")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime meetingTime;

        @NotBlank(message = "모임 장소는 필수입니다")
        private String meetingPlaceName;

        @NotNull(message = "최대 인원은 필수입니다")
        @Max(value = 100, message = "최대 인원은 100명을 초과할 수 없습니다")
        private Integer maxParticipants;

        @NotNull
        private Integer categoryId;

        @NotNull
        private Integer mainLanguageId;

        @NotNull
        private Integer exchangeLanguageId;




    }


    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long meetingId;
        private String meetingTitle;
        private LocalDateTime meetingTime;
        private String meetingPlaceName;
        private Integer currentParticipants;
        private Integer maxParticipants;
        private Integer categoryId;
        private Integer mainLanguageId;
        private Integer exchangeLanguageId;
        private Integer status;
        private List<Long> participantIds;
        private Long hostId;

        public static Response from(Meeting meeting) {
            return Response.builder()
                    .meetingId(meeting.getMeetingId())
                    .meetingTitle(meeting.getMeetingTitle())
                    .meetingTime(meeting.getMeetingTime())
                    .meetingPlaceName(meeting.getMeetingPlaceName())
                    .currentParticipants(meeting.getCurrentParticipants())
                    .maxParticipants(meeting.getMaxParticipants())
                    .categoryId(meeting.getCategoryId())
                    .mainLanguageId(meeting.getMeetingMainLanguageId())
                    .exchangeLanguageId(meeting.getMeetingExchangeLanguageId())
                    .status(meeting.getStatus())
                    .hostId(meeting.getHost().getUserId())
                    .participantIds(meeting.getParticipants().stream()
                            .map(participant -> participant.getUser().getUserId())
                            .collect(Collectors.toList()))
                    .build();
        }
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateResponse {
        private Long meetingId;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvitationRequest {
        @NotNull(message = "초대할 사용자 ID는 필수입니다")
        private Long inviteeId;
    }
}
