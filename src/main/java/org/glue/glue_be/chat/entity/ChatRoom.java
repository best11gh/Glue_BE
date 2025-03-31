package org.glue.glue_be.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.glue.glue_be.common.BaseEntity;
import org.glue.glue_be.meeting.entity.Meeting;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chatroom")
public class ChatRoom extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "chatroom_id")
	private Long chatRoomId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "meeting_id", nullable = false)
	private Meeting meeting;

	@OneToMany(mappedBy = "chatRoom")
	private List<UserChatroom> userChatrooms = new ArrayList<>();

	@Builder
	public ChatRoom(Meeting meeting){
		this.meeting = meeting;
	}

	public void addUserChatroom(UserChatroom userChatroom){
		this.userChatrooms.add(userChatroom);
		userChatroom.updateChatRoom(this);
	}

}
