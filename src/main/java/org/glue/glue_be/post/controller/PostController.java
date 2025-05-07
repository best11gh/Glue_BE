package org.glue.glue_be.post.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.glue.glue_be.auth.jwt.CustomUserDetails;
import org.glue.glue_be.common.response.BaseResponse;
import org.glue.glue_be.post.dto.request.CreatePostRequest;
import org.glue.glue_be.post.dto.response.CreatePostResponse;
import org.glue.glue_be.post.dto.response.GetPostResponse;
import org.glue.glue_be.post.dto.response.GetPostsResponse;
import org.glue.glue_be.post.service.PostService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;


	// 1. 게시글 작성 (로그인 필수)
	@PostMapping
	public BaseResponse<CreatePostResponse> createPost(@RequestBody @Valid CreatePostRequest req, @AuthenticationPrincipal CustomUserDetails auth) {
		String uuid = auth.getUsername();
		return new BaseResponse<>(postService.createPost(req, uuid));
	}


	// 2. 게시글 단건 조회
	@GetMapping("/{postId}")
	public BaseResponse<GetPostResponse> getPost(@PathVariable Long postId) {
		return new BaseResponse<>(postService.getPost(postId));
	}

	// 3. 게시글 수정 (로그인 필수)

	// 4. 게시글 삭제 (로그인 필수)


	// 5. 게시글 끌올 (로그인 필수)
	@GetMapping("/{postId}/bump")
	public BaseResponse<Void> bumpPost(@PathVariable Long postId, @AuthenticationPrincipal CustomUserDetails auth) {
		postService.bumpPost(postId, auth.getUserUuid());
		return new BaseResponse<>();
	}

	// 6. 게시글 목록 조회 (무한 스크롤 / cursor 기반)
	// - bumpedAt 가 있는 글이 먼저 우선적으로 내림차순으로 최근 끌올순 구현
	// - bumpedAt 가 없는 글들 중에선 createdAt 순
	@GetMapping
	public BaseResponse<GetPostsResponse> getPosts(@RequestParam(required = false) Long lastPostId,
		@RequestParam(defaultValue = "10") int size
	) {
		GetPostsResponse response = postService.getPosts(lastPostId, size);
		return new BaseResponse<>(response);
	}

	// 7. 카테고리별 게시글 목록 조회(6과 병합 가능할수도)

	// 8. 검색 결과 게시글 목록 조회


	// 9. 좋아요 등록(토글) (로그인 필수)
	@GetMapping("/{postId}/like")
	public BaseResponse<Void> toggleLike(@PathVariable Long postId, @AuthenticationPrincipal CustomUserDetails auth) {
		postService.toggleLike(postId, auth.getUserUuid());
		return new BaseResponse<>();
	}

}
