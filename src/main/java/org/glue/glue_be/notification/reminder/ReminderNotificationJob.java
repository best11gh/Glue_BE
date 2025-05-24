package org.glue.glue_be.notification.reminder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.notification.dto.request.CreateNotificationRequest;
import org.glue.glue_be.notification.service.NotificationService;
import org.glue.glue_be.user.entity.User;
import org.glue.glue_be.user.repository.UserRepository;
import org.glue.glue_be.user.response.UserResponseStatus;
import org.quartz.*;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReminderNotificationJob extends QuartzJobBean {

    private final NotificationService notificationService;
    private final UserRepository userRepository;


    @Override
    public void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getMergedJobDataMap();

        Long userId = data.getLong("userId");
        Long postId = data.getLong("postId");
        Long hostId = data.getLong("hostId");
        String type = data.getString("type"); // before, after 중 1

        log.info("[Reminder 알림] userId={}, postId={}, type={}, hostId={}", userId, postId, type, hostId);

        User host = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(UserResponseStatus.USER_NOT_FOUND));

        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .receiverId(userId)
                .type("post")
                .title(type.equals("before") ? "모임 만남 잊지 않으셨죠?" : "방명록을 남겨보세요")
                .content(type.equals("before") ? "내일은 " + host.getNickname() + " 님의 모임 만남 예정일이에요!"
                        : "어제 만난 모임의 친구들에게 방명록을 남겨보세요!")
                .targetId(postId)
                .build();

        notificationService.create(request);
    }


}
