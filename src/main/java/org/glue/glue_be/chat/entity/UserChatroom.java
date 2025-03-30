package org.glue.glue_be.chat.entity;


import jakarta.persistence.*;
import lombok.*;
import org.glue.glue_be.common.BaseEntity;
import org.glue.glue_be.user.entity.User;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_chatroom")
public class UserChatroom extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_chatroom_id")
	private Long userChatroomId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chatRoom_id", nullable = false)
	private ChatRoom chatRoom;

	@Builder
	private UserChatroom(User user, ChatRoom chatRoom){
		this.user = user;
		this.chatRoom = chatRoom;
	}

	// user에서 쓸 양방향 동기화 메서드
	void updateUser(User user){ this.user = user; }

	// chatroom에서 쓸 양방향 동기화 메서드
	void updateChatRoom(ChatRoom chatRoom) { this.chatRoom = chatRoom; }



}
