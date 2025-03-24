package org.glue.glue_be.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.glue.glue_be.meeting.entity.Meeting;


@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "chatroom")
public class ChatRoom {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "chatroom_id")
	private Long id;

	// chatroom이 비즈니스적으로 meeting에 속하기 때문에 여기가 FK를 가짐
	// cascade는 meeting과의 관계에 대해 명확히 알게되면 추가해야 할 듯
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "meeting_id", unique = true, nullable = false)
	private Meeting meeting;

}
