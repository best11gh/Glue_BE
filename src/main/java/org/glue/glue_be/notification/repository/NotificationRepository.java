package org.glue.glue_be.notification.repository;

import java.util.List;
import org.glue.glue_be.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByReceiver_UserIdOrderByNotificationIdDesc(Long userId, Pageable pageable);

    List<Notification> findByReceiver_UserIdAndNotificationIdLessThanOrderByNotificationIdDesc(Long userId,
                                                                                               Long cursorId,
                                                                                               Pageable pageable);

}
