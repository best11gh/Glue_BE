package org.glue.glue_be.meeting.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meeting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "meeting_title", nullable = false)
    private String title;

    @OneToMany(mappedBy = "meeting")
    // Cascade, orphanRemoval 사용하지 않음 → 삭제는 서비스 레이어에서 명시적으로 처리해야 함
    private List<Participant> participants = new ArrayList<>();


    @Column(name = "meeting_time", nullable = false)
    private LocalDateTime meetingTime;

    @Column(name = "meeting_place", nullable = false)
    private String meetingPlace;

    @Column(name = "min_ppl", nullable = false)
    private Integer minPpl;

    @Column(name = "max_ppl", nullable = false)
    private Integer maxPpl;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "meeting_place_latitude", nullable = true)
    private Double meetingPlaceLatitude;

    @Column(name = "meeting_place_longitude", nullable = true)
    private Double meetingPlaceLongitude;

    @Column(name = "meeting_place_name", nullable = true)
    private String meetingPlaceName;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPlace(String place, Double latitude, Double longitude, String placeName) {
        this.meetingPlace = place;
        this.meetingPlaceLatitude = latitude;
        this.meetingPlaceLongitude = longitude;
        this.meetingPlaceName = placeName;
    }

    public void setParticipantLimit(int min, int max) {
        this.minPpl = min;
        this.maxPpl = max;
    }

    public void setMeetingTime(LocalDateTime newTime) {
        this.meetingTime = newTime;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}