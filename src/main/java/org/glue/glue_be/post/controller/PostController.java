package org.glue.glue_be.post.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.glue.glue_be.auth.jwt.CustomUserDetails;
import org.glue.glue_be.common.response.BaseResponse;
import org.glue.glue_be.post.dto.request.CreatePostRequest;
import org.glue.glue_be.post.dto.response.BumpPostResponse;
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
		return new BaseResponse<>(postService.createPost(req, auth.getUserId()));
	}


	// 2. 게시글 단건 조회
	@GetMapping("/{postId}")
	public BaseResponse<GetPostResponse> getPost(@PathVariable Long postId, @AuthenticationPrincipal CustomUserDetails auth) {
		return new BaseResponse<>(postService.getPost(postId, auth.getUserId()));
	}

	// 3. 게시글 수정 (로그인 필수)

	// 4. 게시글 삭제 (로그인 필수)
	@DeleteMapping("/{postId}")
	public BaseResponse<Void> deletePost(@PathVariable Long postId, @AuthenticationPrincipal CustomUserDetails auth) {
		postService.deletePost(postId, auth.getUserId());
		return new BaseResponse<>();
	}


	// 5. 게시글 끌올 (로그인 필수)
	@GetMapping("/{postId}/bump")
	public BaseResponse<BumpPostResponse> bumpPost(@PathVariable Long postId, @AuthenticationPrincipal CustomUserDetails auth) {
		return new BaseResponse<>(postService.bumpPost(postId, auth.getUserId()));
	}

	// 6. 게시글 목록 조회 (무한 스크롤 / cursor 기반)
	// - bumpedAt 가 있는 글이 먼저 우선적으로 내림차순으로 최근 끌올순 구현
	// - bumpedAt 가 없는 글들 중에선 createdAt 순
	@GetMapping
	public BaseResponse<GetPostsResponse> getPosts(@RequestParam(required = false) Long lastPostId,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(required = false) Integer categoryId,
		@AuthenticationPrincipal CustomUserDetails auth
	) {
		GetPostsResponse response = postService.getPosts(lastPostId, size, categoryId, auth.getUserId());
		return new BaseResponse<>(response);
	}

	// 7. 검색 결과 게시글 목록 조회
	@GetMapping("/search")
	public BaseResponse<GetPostsResponse> searchPosts(
		@RequestParam String keyword, @RequestParam(required = false) Long lastPostId,
		@RequestParam(defaultValue = "10") int size,
		@AuthenticationPrincipal CustomUserDetails auth
	) {
		return new BaseResponse<>(postService.searchPosts(lastPostId, size, keyword, auth.getUserId()));
	}


	// 8. 좋아요 등록(토글) (로그인 필수)
	@GetMapping("/{postId}/like")
	public BaseResponse<Void> toggleLike(@PathVariable Long postId, @AuthenticationPrincipal CustomUserDetails auth) {
		postService.toggleLike(postId, auth.getUserId());
		return new BaseResponse<>();
	}

}
