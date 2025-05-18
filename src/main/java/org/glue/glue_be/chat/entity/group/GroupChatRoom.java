package org.glue.glue_be.chat.entity.group;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.glue.glue_be.common.BaseEntity;
import org.glue.glue_be.meeting.entity.Meeting;

import java.util.List;

@Entity
@Table(name = "group_chatroom")
@Getter
@NoArgsConstructor
public class GroupChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupChatroomId;

    @ManyToOne
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    @OneToMany(mappedBy = "groupChatroom")
    private List<GroupUserChatRoom> groupUserChatrooms;

    @OneToMany(mappedBy = "groupChatroom")
    private List<GroupMessage> messages;

    // Constructor
    public GroupChatRoom(Meeting meeting) {
        this.meeting = meeting;
    }

    // Methods to update entity state
    public void updateMeeting(Meeting meeting) {
        this.meeting = meeting;
    }
}