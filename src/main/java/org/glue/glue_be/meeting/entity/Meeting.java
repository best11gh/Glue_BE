package org.glue.glue_be.meeting.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.Builder;
import org.glue.glue_be.common.BaseEntity;
import org.glue.glue_be.user.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Entity
@Table(name = "meeting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Meeting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_id", nullable = false, updatable = false)
    private Long meetingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Column(name = "meeting_title", nullable = false)
    private String meetingTitle;

    @OneToMany(mappedBy = "meeting")
    private List<Participant> participants = new ArrayList<>();

    public static final long UPDATE_LIMIT_HOUR = 3; // 모임 수정이 불가능한 남은 모임시간

    @Column(name = "meeting_time", nullable = false)
    private LocalDateTime meetingTime;

    @Column(name = "current_participants", nullable = false)
    private Integer currentParticipants;

    @Column(name = "min_participants", nullable = false)
    private Integer minParticipants;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "meeting_place_name")
    private String meetingPlaceName;

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Column(name = "meeting_main_language_id", nullable = false)
    private Integer meetingMainLanguageId;

    @Column(name = "meeting_exchange_language_id", nullable = false)
    private Integer meetingExchangeLanguageId;

    @Column(name = "meeting_image_url")
    private String meetingImageUrl;

    @Builder
    private Meeting(User host,
                    String meetingTitle,
                    LocalDateTime meetingTime,
                    Integer currentParticipants,
                    Integer minParticipants,
                    Integer maxParticipants,
                    Integer status,
                    String meetingPlaceName,
                    Integer categoryId,
                    Integer meetingMainLanguageId,
                    Integer meetingExchangeLanguageId,
                    String meetingImageUrl
        ) {
        this.host = host;
        this.meetingTitle = meetingTitle;
        this.meetingTime = meetingTime;
        this.currentParticipants = currentParticipants;
        this.minParticipants = minParticipants;
        this.maxParticipants = maxParticipants;
        this.status = status;
        this.meetingPlaceName = meetingPlaceName;
        this.participants = new ArrayList<>();
        this.categoryId = categoryId;
        this.meetingMainLanguageId = meetingMainLanguageId;
        this.meetingExchangeLanguageId = meetingExchangeLanguageId;
        this.meetingImageUrl = meetingImageUrl;
    }

    public List<Participant> getParticipants() {
        return Collections.unmodifiableList(participants);
    }


    // 수정 api에서 meeting쪽 속성 일괄 변경하는 메서드
    public void updateMeeting(String newTitle, String newPlaceName, LocalDateTime newMeetingTime, Integer newMainLanguageId, Integer newExchangeLanguageId, Integer newMaxParticipants) {
        this.meetingTitle = newTitle;
        this.meetingPlaceName = newPlaceName;
        this.meetingTime = newMeetingTime;
        this.meetingMainLanguageId = newMainLanguageId;
        this.meetingExchangeLanguageId = newExchangeLanguageId;
        this.maxParticipants = newMaxParticipants;
    }

    public void changeStatus(int newStatus) {this.status = newStatus;}

    public void changeImageUrl(String newImageUrl) {
        this.meetingImageUrl = newImageUrl;
    }


    /**
     * 미팅을 활성화 상태로 변경합니다.
     * 상태 코드 1은 활성화 상태를 의미합니다.
     */
    public void activateMeeting() {
        this.status = 1; // 1: 활성화 상태
    }

    public void addParticipant(Participant participant) {
        this.participants.add(participant);
        this.currentParticipants++;
        participant.updateMeeting(this);
    }

    public boolean isMeetingFull() {
        return this.participants.size() >= this.maxParticipants;
    }

    public boolean isHost(Long userId) {
        return this.host.getUserId().equals(userId);
    }
}
