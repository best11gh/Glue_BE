package org.glue.glue_be.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.glue.glue_be.common.dto.MeetingSummary;
import org.glue.glue_be.common.dto.UserSummary;
import org.glue.glue_be.common.dto.UserSummaryWithHostInfo;

import java.time.LocalDateTime;
import java.util.List;

@Builder(toBuilder = true)
public record GroupChatRoomDetailResponse(
        Long groupChatroomId,
        MeetingSummary meeting,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        List<UserSummary> participants,

        @JsonProperty("participants")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        List<UserSummaryWithHostInfo> participantsWithHostInfo,

        LocalDateTime createdAt,
        Integer pushNotificationOn
) {
    // JSON 직렬화 시 participants 또는 participantsWithHostInfo 중 하나만 사용
    @JsonProperty("participants")
    public Object getParticipantsForSerialization() {
        return participantsWithHostInfo != null ? participantsWithHostInfo : participants;
    }

    public GroupChatRoomDetailResponse withHostInfo(List<UserSummaryWithHostInfo> participantsWithHostInfo) {
        return new GroupChatRoomDetailResponse(
                groupChatroomId,
                meeting,
                null, // 기존 participants 필드를 null로 설정
                participantsWithHostInfo,
                createdAt,
                pushNotificationOn
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long groupChatroomId;
        private MeetingSummary meeting;
        private List<UserSummary> participants;
        private List<UserSummaryWithHostInfo> participantsWithHostInfo;
        private LocalDateTime createdAt;
        private Integer pushNotificationOn;

        public Builder groupChatroomId(Long groupChatroomId) {
            this.groupChatroomId = groupChatroomId;
            return this;
        }

        public Builder meeting(MeetingSummary meeting) {
            this.meeting = meeting;
            return this;
        }

        public Builder participants(List<UserSummary> participants) {
            this.participants = participants;
            return this;
        }

        public Builder participantsWithHostInfo(List<UserSummaryWithHostInfo> participantsWithHostInfo) {
            this.participantsWithHostInfo = participantsWithHostInfo;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder pushNotificationOn(Integer pushNotificationOn) {
            this.pushNotificationOn = pushNotificationOn;
            return this;
        }

        public GroupChatRoomDetailResponse build() {
            return new GroupChatRoomDetailResponse(
                    groupChatroomId,
                    meeting,
                    participants,
                    participantsWithHostInfo,
                    createdAt,
                    pushNotificationOn
            );
        }
    }
}