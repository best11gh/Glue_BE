package org.glue.glue_be.meeting.dto;

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
        private LocalDateTime meetingTime;

        @NotBlank(message = "모임 장소는 필수입니다")
        private String meetingPlaceName;

        private Double meetingPlaceLatitude;
        private Double meetingPlaceLongitude;

        @NotNull(message = "최소 인원은 필수입니다")
        @Min(value = 1, message = "최소 인원은 1명 이상이어야 합니다")
        private Integer minPpl;

        @NotNull(message = "최대 인원은 필수입니다")
        @Max(value = 100, message = "최대 인원은 100명을 초과할 수 없습니다")
        private Integer maxPpl;
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
        private Double meetingPlaceLatitude;
        private Double meetingPlaceLongitude;
        private Integer currentParticipants;
        private Integer minParticipants;
        private Integer maxParticipants;
        private Integer status;
        private List<Long> participantIds;
        private Long hostId;

        public static Response from(Meeting meeting) {
            return Response.builder()
                    .meetingId(meeting.getMeetingId())
                    .meetingTitle(meeting.getMeetingTitle())
                    .meetingTime(meeting.getMeetingTime())
                    .meetingPlaceName(meeting.getMeetingPlaceName())
                    .meetingPlaceLatitude(meeting.getMeetingPlaceLatitude())
                    .meetingPlaceLongitude(meeting.getMeetingPlaceLongitude())
                    .currentParticipants(meeting.getCurrentParticipants())
                    .minParticipants(meeting.getMinParticipants())
                    .maxParticipants(meeting.getMaxParticipants())
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
