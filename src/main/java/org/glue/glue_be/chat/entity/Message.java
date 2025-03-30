package org.glue.glue_be.chat.entity;


import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "message")
public class Message {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "message_id")
	private Long id;

	// Message와 User는 매개 테이블 없이 직접적인 연관관계를 매핑
	// 따라서 반대측인 User에서 OneToMany를 작성할 필요 없음
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chatroom_id", nullable = false)
	private ChatRoom chatRoom;

	@Column(name = "message_content", columnDefinition = "TEXT", nullable = false)
	private String messageContent;

}
