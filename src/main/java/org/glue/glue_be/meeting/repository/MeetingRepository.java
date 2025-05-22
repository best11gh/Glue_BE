package org.glue.glue_be.meeting.repository;

import org.glue.glue_be.meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    Meeting findByMeetingId(Long meetingId);

    List<Meeting> findByHost_UserId(Long userId);

    @Modifying
    @Query(value = """
        UPDATE meeting\s
        SET meeting_image_url = (
            SELECT pi.image_url
            FROM post p
            JOIN post_image pi ON p.post_id = pi.post_id
            WHERE p.meeting_id = meeting.meeting_id
            AND pi.image_order = 0
            ORDER BY p.post_id DESC
            LIMIT 1
        )
        WHERE meeting_id = :meetingId
        """, nativeQuery = true)
    void updateMeetingImageUrl(@Param("meetingId") Long meetingId);
}
