package org.glue.glue_be.post.dto.response;



import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreatePostResponse {

	private Long postId;

}
