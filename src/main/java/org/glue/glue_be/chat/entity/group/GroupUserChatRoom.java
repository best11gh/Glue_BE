package org.glue.glue_be.chat.entity.group;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.glue.glue_be.common.BaseEntity;
import org.glue.glue_be.meeting.entity.Meeting;
import org.glue.glue_be.user.entity.User;

@Entity
@Table(name = "group_user_chatroom")
@Getter
@NoArgsConstructor
public class GroupUserChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "group_chatroom_id")
    private GroupChatRoom groupChatroom;

    @Column(name = "last_read_message_id")
    private Long lastReadMessageId = 0L;

    private Integer pushNotificationOn = 1;

    // Constructor with required fields
    public GroupUserChatRoom(User user, GroupChatRoom groupChatroom) {
        this.user = user;
        this.groupChatroom = groupChatroom;
        this.pushNotificationOn = 1; // Default value
    }

    public void togglePushNotification(Integer pushNotificationOn) {
        this.pushNotificationOn = pushNotificationOn;
    }

    public void setPushNotification(boolean enabled) {
        this.pushNotificationOn = enabled ? 1 : 0;
    }
}