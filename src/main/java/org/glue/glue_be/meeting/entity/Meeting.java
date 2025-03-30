package org.glue.glue_be.meeting.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "meeting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_id", nullable = false, updatable = false)
    private Long meetingId;

    @Column(name = "meeting_title", nullable = false)
    private String meetingTitle;

    @OneToMany(mappedBy = "meeting")
    private List<Participant> participants = new ArrayList<>();

    @Column(name = "meeting_time", nullable = false)
    private LocalDateTime meetingTime;

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

    @Builder
    private Meeting(String meetingTitle,
                    LocalDateTime meetingTime,
                    Integer minPpl,
                    Integer maxPpl,
                    Integer status,
                    Double meetingPlaceLatitude,
                    Double meetingPlaceLongitude,
                    String meetingPlaceName) {
        if (meetingTitle == null || meetingTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("Meeting title cannot be null or empty");
        }
        if (meetingTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Meeting time must be in the future");
        }
        if (minPpl < 1 || minPpl > maxPpl) {
            throw new IllegalArgumentException("Invalid participant capacity range");
        }
        if (status < 1 || status > 3) {
            throw new IllegalArgumentException("Invalid meeting status");
        }
        this.meetingTitle = meetingTitle;
        this.meetingTime = meetingTime;
        this.minPpl = minPpl;
        this.maxPpl = maxPpl;
        this.status = status;
        this.meetingPlaceLatitude = meetingPlaceLatitude;
        this.meetingPlaceLongitude = meetingPlaceLongitude;
        this.meetingPlaceName = meetingPlaceName;
        this.participants = new ArrayList<>();
    }

    public List<Participant> getParticipants() {
        return Collections.unmodifiableList(participants);
    }

    public void changeTitle(String newTitle) {
        if (newTitle == null || newTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("Meeting title cannot be null or empty");
        }
        this.meetingTitle = newTitle;
    }

    public void changeLocation(Double latitude, Double longitude, String placeName) {
        if (latitude != null) {
            if (latitude < -90 || latitude > 90) {
                throw new IllegalArgumentException("Latitude must be between -90 and 90");
            }
        }
        if (longitude != null) {
            if (longitude < -180 || longitude > 180) {
                throw new IllegalArgumentException("Longitude must be between -180 and 180");
            }
        }
        if (placeName != null && placeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Place name, if provided, cannot be empty");
        }
        this.meetingPlaceLatitude = latitude;
        this.meetingPlaceLongitude = longitude;
        this.meetingPlaceName = placeName;
    }


    public void changeMinimumCapacity(int newMinPpl) {
        if (newMinPpl < 1) {
            throw new IllegalArgumentException("Minimum people must be at least 1");
        }
        if (newMinPpl > this.maxPpl) {
            throw new IllegalArgumentException("Minimum people cannot be greater than maximum people");
        }
        this.minPpl = newMinPpl;
    }

    public void changeMaximumCapacity(int newMaxPpl) {
        if (newMaxPpl < this.minPpl) {
            throw new IllegalArgumentException("Maximum people cannot be less than minimum people");
        }
        this.maxPpl = newMaxPpl;
    }

    public void rescheduleMeeting(LocalDateTime newTime) {
        if (newTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Meeting time must be in the future");
        }
        this.meetingTime = newTime;
    }

    public void changeStatus(int newStatus) {
        if (newStatus < 1 || newStatus > 3) {
            throw new IllegalArgumentException("Invalid meeting status");
        }
        this.status = newStatus;
    }

    public void addParticipant(Participant participant) {
        if (this.participants.size() >= this.maxPpl) {
            throw new IllegalStateException("Cannot add more participants than maximum allowed");
        }
        this.participants.add(participant);
        participant.updateMeeting(this);
    }

    public void removeParticipant(Participant participant) {
        if (!this.participants.contains(participant)) {
            throw new IllegalArgumentException("Participant not found in the meeting");
        }
        this.participants.remove(participant);
        participant.updateMeeting(null);
    }

    public boolean isMeetingFull() {
        return this.participants.size() >= this.maxPpl;
    }
}
