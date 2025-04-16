package org.glue.glue_be.chat.entity.dm;

import jakarta.persistence.*;
import lombok.*;
import org.glue.glue_be.common.BaseEntity;
import org.glue.glue_be.user.entity.User;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "dm_message")
public class DmMessage extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "dm_message_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dm_chatroom_id", nullable = false)
	private DmChatRoom dmChatRoom;

	//TODO: 추후 이부분을 일반 속성인 uuid로 대체해야 할지 결정해야함
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "dm_message_content", columnDefinition = "TEXT", nullable = false)
	private String dmMessageContent;

	@Column(name = "is_read", nullable = false)
	@ColumnDefault("0")
	private int isRead;

	// isRead를 boolean으로 바꾸기 (repository에서 쉽게 사용하게 하기 위함)
	public boolean isRead() {
		return isRead == 1;
	}

	public void setIsRead(int isRead) {
		this.isRead = isRead;
	}

	@Builder
	private DmMessage(DmChatRoom chatRoom, User user, String dmMessageContent){
		this.dmChatRoom = chatRoom;
		this.user = user;
		this.dmMessageContent = dmMessageContent;
	}

	public void changedmMessageContent(String newDmMessageContent) { this.dmMessageContent = newDmMessageContent; }

}
