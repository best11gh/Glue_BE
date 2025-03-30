package org.glue.glue_be.meeting.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.glue.glue_be.user.entity.User;

@Entity
@Table(name = "participant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id", updatable = false)
    private Long id;

    // Many Participants → One User
    // User에서는 OneToMany(mappedBy = "user")로써 Participant.user 필드를 기준으로 연관관계가 매핑됨을 표현해야 함
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Many Participants → One Meeting
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    void setMeeting(Meeting meeting) {
        this.meeting = meeting;
    }

    void setUser(User user) {
        this.user = user;
    }

    // 양방향 정합성을 위해
    //    public void addParticipant(Participant participant) {
    //        participants.add(participant);
    //        participant.setUser(this);
    //    }
    //
    //    public void addParticipant(Participant participant) {
    //        participants.add(participant);
    //        participant.setMeeting(this);
    //    }
    // 를 User, Meeting 측에 추가하면 좋을 듯

}
