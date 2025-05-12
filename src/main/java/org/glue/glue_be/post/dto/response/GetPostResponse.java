package org.glue.glue_be.post.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Builder
public class GetPostResponse {

	private MeetingDto meeting;

	private PostDto post;

	@Getter
	@Builder
	public static class MeetingDto {

		private Long meetingId;

		private Integer categoryId;

		private String creatorName;

		private Long creatorId;

		private String creatorImageUrl;

		@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
		private LocalDateTime meetingTime;

		private Integer currentParticipants;

		private Integer maxParticipants;

		private Integer languageId;

		private Integer meetingStatus;

		private List<ParticipantDto> participants;

		@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
		private LocalDateTime createdAt;

		@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
		private LocalDateTime updatedAt;


		@Getter
		@Builder
		public static class ParticipantDto {

			private Long userId;

			private String nickname;

			private String profileImageUrl;

		}
	}

	@Getter
	@Builder
	public static class PostDto {

		private Long postId;

		private String title;

		private String content;

		private Integer viewCount;

		@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
		private LocalDateTime bumpedAt;

		private Integer likeCount;

		private List<PostImageDto> postImageUrl;


		// todo: common으로 옮기기
		@Getter
		@Builder
		public static class PostImageDto {
			private Long postImageId;
			private String imageUrl;
			private Integer imageOrder;

		}

	}


}
