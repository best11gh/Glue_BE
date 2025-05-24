package org.glue.glue_be.user.dto.response;


import lombok.Builder;
import lombok.Getter;
import org.glue.glue_be.meeting.entity.Meeting;
import org.glue.glue_be.post.entity.Post;

import java.util.List;


@Getter
@Builder
public class MeetingHistoryResponse {

	private List<MeetingHistoryItem> hostedMeetings;

	private List<MeetingHistoryItem> joinedMeetings;

	// 정적 팩토리 메서드 추가
	public static MeetingHistoryItem convertToMeetingHistoryItem(Post post) {
		Meeting meeting = post.getMeeting();
		return MeetingHistoryItem.builder()
			.postId(post.getId())
			.meetingThumbnail(post.getImages().get(0).getImageUrl())
			.meetingTitle(meeting.getMeetingTitle())
			.categoryId(meeting.getCategoryId())
			.build();
	}



	@Getter
	@Builder
	public static class MeetingHistoryItem {

		private Long postId;

		private String meetingThumbnail;

		private String meetingTitle;

		private Integer categoryId;

	}
}
