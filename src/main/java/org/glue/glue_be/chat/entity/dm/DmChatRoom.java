package org.glue.glue_be.chat.entity.dm;

import jakarta.persistence.*;
import lombok.*;
import org.glue.glue_be.common.BaseEntity;
import org.glue.glue_be.meeting.entity.Meeting;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "dm_chatroom")
public class DmChatRoom extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "dm_chatroom_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "meeting_id", nullable = false)
	private Meeting meeting;

	@OneToMany(mappedBy = "dmChatRoom")
	private List<DmUserChatroom> dmUserChatrooms = new ArrayList<>();

	@Builder
	public DmChatRoom(Meeting meeting){
		this.meeting = meeting;
	}

	public void addUserChatroom(DmUserChatroom dmUserChatroom){
		this.dmUserChatrooms.add(dmUserChatroom);
		dmUserChatroom.updateChatRoom(this);
	}

	public void updateLastActivity() {
		this.touchUpdatedAt();
	}
}
