package org.glue.glue_be.post.dto.response;


import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Builder
public class GetPostsResponse {

	private Boolean hasNext; // 더 가져올 데이터가 있는지 여부

	private List<PostItem> posts;

	@Getter
	@Builder
	public static class PostItem {

		private Long postId;

		private Integer viewCount;

		private Integer categoryId;

		private String title;

		private String content;

		private Integer likeCount;

		private Integer currentParticipants;

		private Integer maxParticipants;

		private LocalDateTime createdAt;

		private String thumbnailUrl;

	}

}