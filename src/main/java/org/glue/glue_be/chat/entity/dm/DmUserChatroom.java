package org.glue.glue_be.chat.entity.dm;

import jakarta.persistence.*;
import lombok.*;
import org.glue.glue_be.common.BaseEntity;
import org.glue.glue_be.user.entity.User;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "dm_user_chatroom")
public class DmUserChatroom extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "dm_user_chatroom_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dm_chatroom_id", nullable = false)
	private DmChatRoom dmChatRoom;

	@Builder
	private DmUserChatroom(User user, DmChatRoom dmChatRoom){
		this.user = user;
		this.dmChatRoom = dmChatRoom;
	}

	void updateUser(User user){ this.user = user; }

	void updateChatRoom(DmChatRoom dmChatRoom) { this.dmChatRoom = dmChatRoom; }
}
