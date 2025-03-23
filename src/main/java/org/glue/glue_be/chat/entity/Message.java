package org.glue.glue_be.chat.entity;


import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "message")
public class Message {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "message_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chatroom_id", nullable = false)
	private ChatRoom chatRoom;

	@Column(name = "message_content", columnDefinition = "TEXT", nullable = false)
	private String messageContent;

}
