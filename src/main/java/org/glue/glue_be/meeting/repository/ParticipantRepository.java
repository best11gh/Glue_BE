package org.glue.glue_be.meeting.repository;

import org.glue.glue_be.meeting.entity.Meeting;
import org.glue.glue_be.meeting.entity.Participant;
import org.glue.glue_be.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    Optional<Participant> findByUserAndMeeting(User user, Meeting meeting);
    boolean existsByUserAndMeeting(User user, Meeting meeting);
} 