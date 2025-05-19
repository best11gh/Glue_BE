package org.glue.glue_be.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.glue.glue_be.common.dto.UserSummary;
import org.glue.glue_be.common.dto.UserSummaryWithHostInfo;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DmChatRoomDetailResponse {
    private Long dmChatRoomId;
    private Long meetingId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<UserSummary> participants;

    // 추가: 호스트 정보가 있는 참가자 목록 (JSON 직렬화 시 participants와 같은 필드명 사용)
    @JsonProperty("participants")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<UserSummaryWithHostInfo> participantsWithHostInfo;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer isPushNotificationOn;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer invitationStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * JSON 직렬화 시 participants 또는 participantsWithHostInfo 중 하나만 사용
     * @return 적절한 참가자 목록
     */
    @JsonProperty("participants")
    public Object getParticipantsForSerialization() {
        return participantsWithHostInfo != null ? participantsWithHostInfo : participants;
    }

    /**
     * 호스트 정보가 포함된 응답으로 변환
     * @param participantsWithHostInfo 호스트 정보가 포함된 참가자 목록
     * @return 새 응답 객체
     */
    public DmChatRoomDetailResponse withHostInfo(List<UserSummaryWithHostInfo> participantsWithHostInfo) {
        return toBuilder()
                .participants(null) // 기존 participants 필드를 null로 설정
                .participantsWithHostInfo(participantsWithHostInfo)
                .build();
    }

    // participantsWithHostInfo 필드를 위한 빌더 메서드 추가
    public static class DmChatRoomDetailResponseBuilder {
        public DmChatRoomDetailResponseBuilder participantsWithHostInfo(List<UserSummaryWithHostInfo> participantsWithHostInfo) {
            this.participantsWithHostInfo = participantsWithHostInfo;
            return this;
        }
    }
}