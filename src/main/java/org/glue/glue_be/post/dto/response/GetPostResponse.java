package org.glue.glue_be.post.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import org.glue.glue_be.post.entity.PostImage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


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

		private UUID creatorUuid;

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


		@Getter
		@Builder
		public static class PostImageDto {
			private Long postImageId;
			private String imageUrl;
			private Integer imageOrder;

		}

	}


}
