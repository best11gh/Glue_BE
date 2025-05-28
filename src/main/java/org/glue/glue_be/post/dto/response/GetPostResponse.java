package org.glue.glue_be.post.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import org.glue.glue_be.common.dto.UserSummary;

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

		private UserSummary creator;

		@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
		private LocalDateTime meetingTime;

		private Integer currentParticipants;

		private Integer maxParticipants;

		private String meetingPlaceName;

		private Integer mainLanguageId;

		private Integer exchangeLanguageId;

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

		// 끌올 직전 유저에게 몇번 했는지 알려주기 위해 끌올 카운트 포함
		private Integer bumpedCount;
		private Integer bumpLimit;

		private Integer likeCount;
		private Boolean isLiked;

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
