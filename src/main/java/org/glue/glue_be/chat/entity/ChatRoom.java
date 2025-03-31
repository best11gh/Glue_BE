package org.glue.glue_be.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.glue.glue_be.meeting.entity.Meeting;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chatroom")
public class ChatRoom {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "chatroom_id")
	private Long chatRoomId;

	// chatroom이 비즈니스적으로 meeting에 속하기 때문에 여기가 FK를 가짐
	// cascade는 meeting과의 관계에 대해 명확히 알게되면 추가해야 할 듯
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "meeting_id", nullable = false)
	private Meeting meeting;

	// ChatRoom과 UserChatroom 간의 일대다 관계 매핑
	@OneToMany(mappedBy = "chatRoom")
	private List<UserChatroom> userChatrooms = new ArrayList<>();

	// 생성자를 따로 만들고 Builder를 붙임 -> Allargs 배제
	@Builder
	public ChatRoom(Meeting meeting){
		this.meeting = meeting;
	}

	// userChatrooms에 넣고 userChatroom의 메서드에 this를 넣어 동기화
	public void addUserChatroom(UserChatroom userChatroom){
		this.userChatrooms.add(userChatroom);
		userChatroom.updateChatRoom(this);
	}

}
