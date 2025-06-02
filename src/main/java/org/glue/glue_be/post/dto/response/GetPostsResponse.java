package org.glue.glue_be.post.dto.response;


import lombok.Builder;
import lombok.Getter;
import org.glue.glue_be.post.entity.Post;

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

		// 좋아요 상태 상수
		public static final int LIKED = 1;
		public static final int NOT_LIKED = 0;

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

		// 현재 로그인한 유저가 이 게시글에 좋아요를 눌렀는지 1(예), 0(아니오)
		private Integer isUserLikedThisPost;

	}

	// 엔티티 → 목록 itemDTO 변환용 메서드
	public static PostItem ofEntity(Post p, boolean isLiked) {
		return PostItem.builder()
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
			.isUserLikedThisPost(isLiked ? PostItem.LIKED : PostItem.NOT_LIKED)
			.build();
	}

}