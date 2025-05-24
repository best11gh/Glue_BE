package org.glue.glue_be.notification.reminder;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.post.entity.Post;
import org.glue.glue_be.post.repository.PostRepository;
import org.glue.glue_be.post.response.PostResponseStatus;
import org.quartz.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReminderSchedulerService {

    private final Scheduler scheduler;
    private final PostRepository postRepository;


    // 리마인더 재등록
    public void rescheduleReminder(Long userId, Long postId, LocalDateTime newMeetingTime) {
        deleteReminder(userId, postId, "before");
        deleteReminder(userId, postId, "after");
        scheduleReminder(userId, postId, newMeetingTime);
    }

    // 리마인더 생성
    public void scheduleReminder(Long userId, Long postId, LocalDateTime meetingTime) {
        LocalDateTime now = LocalDateTime.now();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BaseException(PostResponseStatus.POST_NOT_FOUND));
        Long hostId = post.getMeeting().getHost().getUserId();

        // 모임이 24시간 이상 남았을 경우에만 before 리마인더 설정
        if (now.isBefore(meetingTime.minusHours(24))) {
            scheduleJob(userId, postId, hostId, meetingTime.minusHours(24), "before");
        }

        scheduleJob(userId, postId, hostId, meetingTime.plusHours(24), "after");
    }


    public void removeRemindersByPost(Long postId, List<Long> participantIds) {
        for (Long userId : participantIds) {
            deleteReminder(userId, postId, "before");
            deleteReminder(userId, postId, "after");
        }
    }

    private void scheduleJob(Long userId, Long postId, Long hostId, LocalDateTime time, String type) {
        try {
            JobKey jobKey = generateJobKey(userId, postId, type);
            TriggerKey triggerKey = generateTriggerKey(userId, postId, type);

            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }

            JobDetail jobDetail = JobBuilder.newJob(ReminderNotificationJob.class)
                    .withIdentity(jobKey)
                    .usingJobData("userId", userId)
                    .usingJobData("postId", postId)
                    .usingJobData("hostId", hostId)
                    .usingJobData("type", type)
                    .storeDurably()
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .startAt(Timestamp.valueOf(time))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException("리마인더 예약 실패", e);
        }
    }

    private void deleteReminder(Long userId, Long postId, String type) {
        try {
            scheduler.deleteJob(generateJobKey(userId, postId, type));
        } catch (SchedulerException e) {
            throw new RuntimeException("기존 리마인더 삭제 실패", e);
        }
    }

    private JobKey generateJobKey(Long userId, Long postId, String type) {
        return JobKey.jobKey("reminder_" + userId + "_" + postId + "_" + type);
    }

    private TriggerKey generateTriggerKey(Long userId, Long postId, String type) {
        return TriggerKey.triggerKey("trigger_" + userId + "_" + postId + "_" + type);
    }
}
