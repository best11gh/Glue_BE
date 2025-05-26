package org.glue.glue_be.notification.repository;

import java.util.List;
import org.glue.glue_be.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByReceiver_UserIdAndTypeOrderByNotificationIdDesc(Long userId, String type,
                                                                             Pageable pageable);

    List<Notification> findByReceiver_UserIdAndTypeAndNotificationIdLessThanOrderByNotificationIdDesc(Long userId,
                                                                                                      String type,
                                                                                                      Long cursorId,
                                                                                                      Pageable pageable);

    List<Notification> findByReceiver_UserIdAndTypeInOrderByNotificationIdDesc(Long userId, List<String> types,
                                                                               Pageable pageable);

    List<Notification> findByReceiver_UserIdAndTypeInAndNotificationIdLessThanOrderByNotificationIdDesc(Long userId,
                                                                                                        List<String> types,
                                                                                                        Long cursorId,
                                                                                                        Pageable pageable);


}
