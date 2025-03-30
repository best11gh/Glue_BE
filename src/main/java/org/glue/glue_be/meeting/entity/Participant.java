package org.glue.glue_be.meeting.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.glue.glue_be.user.entity.User;
import lombok.Builder;

@Entity
@Table(name = "participant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id", updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Builder
    private Participant(User user, Meeting meeting) {
        this.user = user;
        this.meeting = meeting;
    }

    void updateMeeting(Meeting meeting) {
        this.meeting = meeting;
    }

    void updateUser(User user) {
        this.user = user;
    }

    public void cancelParticipation() {
        if (this.meeting != null) {
            this.meeting.removeParticipant(this);
        }
    }
}