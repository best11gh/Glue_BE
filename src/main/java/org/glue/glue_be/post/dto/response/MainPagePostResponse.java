package org.glue.glue_be.post.dto.response;


import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;


@Getter
@Builder
public class MainPagePostResponse {
	Integer postId;
	Integer categoryId;
	LocalDateTime createdAt;
	String title;
	String content;
	Integer likeCount;
	Integer isLiked;
	Integer currentParticipants;
	Integer maxParticipants;

		
}
