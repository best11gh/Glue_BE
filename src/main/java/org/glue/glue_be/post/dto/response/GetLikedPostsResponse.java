package org.glue.glue_be.post.dto.response;


import lombok.Builder;
import lombok.Getter;
import org.glue.glue_be.post.entity.Post;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Builder
public class GetLikedPostsResponse {

	private List<GetLikedPostsResponse.PostItem> posts;

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

	// 엔티티 → 목록 itemDTO 변환용 메서드
	public static GetLikedPostsResponse.PostItem ofEntity(Post p) {
		return GetLikedPostsResponse.PostItem.builder()
			.postId(p.getId())
			.viewCount(p.getViewCount())
			.categoryId(p.getMeeting().getCategoryId())
			.title(p.getTitle())
			.content(p.getContent())
			.likeCount(p.getLikes().size())
			.currentParticipants(p.getMeeting().getCurrentParticipants())
			.maxParticipants(p.getMeeting().getMaxParticipants())
			.createdAt(p.getMeeting().getCreatedAt())
			.thumbnailUrl(p.getImages().isEmpty()
				? null
				: p.getImages().get(0).getImageUrl())
			.build();
	}
}
