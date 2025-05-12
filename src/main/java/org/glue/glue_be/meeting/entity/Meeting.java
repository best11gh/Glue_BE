package org.glue.glue_be.meeting.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.Builder;
import org.glue.glue_be.common.BaseEntity;
import org.glue.glue_be.common.config.LocalDateTimeStringConverter;
import org.glue.glue_be.user.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


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

    @Column(name = "meeting_time", nullable = false)
    @Convert(converter = LocalDateTimeStringConverter.class)
    private LocalDateTime meetingTime;

    @Column(name = "current_participants", nullable = false)
    private Integer currentParticipants;

    @Column(name = "min_participants", nullable = false)
    private Integer minParticipants;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "meeting_place_latitude", nullable = true)
    private Double meetingPlaceLatitude;

    @Column(name = "meeting_place_longitude", nullable = true)
    private Double meetingPlaceLongitude;

    @Column(name = "meeting_place_name", nullable = true)
    private String meetingPlaceName;

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Column(name = "language_id", nullable = false)
    private Integer languageId;

    @Builder
    private Meeting(User host,
                    String meetingTitle,
                    LocalDateTime meetingTime,
                    Integer currentParticipants,
                    Integer minParticipants,
                    Integer maxParticipants,
                    Integer status,
                    Double meetingPlaceLatitude,
                    Double meetingPlaceLongitude,
                    String meetingPlaceName,
                    Integer categoryId,
                    Integer languageId) {
        this.host = host;
        this.meetingTitle = meetingTitle;
        this.meetingTime = meetingTime;
        this.currentParticipants = currentParticipants;
        this.minParticipants = minParticipants;
        this.maxParticipants = maxParticipants;
        this.status = status;
        this.meetingPlaceLatitude = meetingPlaceLatitude;
        this.meetingPlaceLongitude = meetingPlaceLongitude;
        this.meetingPlaceName = meetingPlaceName;
        this.participants = new ArrayList<>();
        this.categoryId = categoryId;
        this.languageId = languageId;
    }

    public List<Participant> getParticipants() {
        return Collections.unmodifiableList(participants);
    }

    public void changeTitle(String newTitle) {
        this.meetingTitle = newTitle;
    }

    public void changeLocation(Double latitude, Double longitude, String placeName) {
        this.meetingPlaceLatitude = latitude;
        this.meetingPlaceLongitude = longitude;
        this.meetingPlaceName = placeName;
    }


    public void changeMinimumCapacity(int newMinPpl) {
        this.minParticipants = newMinPpl;
    }

    public void changeMaximumCapacity(int newMaxPpl) {
        this.maxParticipants = newMaxPpl;
    }

    public void rescheduleMeeting(LocalDateTime newTime) {
        this.meetingTime = newTime;
    }

    public void changeStatus(int newStatus) {
        this.status = newStatus;
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
        participant.updateMeeting(this);
    }

    public boolean isMeetingFull() {
        return this.participants.size() >= this.maxParticipants;
    }

    public boolean isHost(Long userId) {
        return this.host.getUserId().equals(userId);
    }
}
