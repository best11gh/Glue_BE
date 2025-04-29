package org.glue.glue_be.meeting.repository;

import org.glue.glue_be.meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    Meeting findByMeetingId(Long meetingId);

    List<Meeting> findByHost_UserId(Long userId);
}
