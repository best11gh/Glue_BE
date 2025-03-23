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


	// chatroom은 meeting과 일대일 매핑관계이다. 단방향으로 가정.
	// chatroom이 비즈니스적으로 meeting에 속할 때 FK는 전자가 가져야 한다고 한다
	// cascade는 meeting과의 관계에 대해 명확히 알게되면 추가해야할듯
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "meeting_id", unique = true, nullable = false)
	private Meeting meeting;

}
