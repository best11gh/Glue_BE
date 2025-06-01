package org.glue.glue_be.post.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.glue.glue_be.auth.jwt.CustomUserDetails;
import org.glue.glue_be.common.response.BaseResponse;
import org.glue.glue_be.post.dto.request.CreatePostRequest;
import org.glue.glue_be.post.dto.request.UpdatePostRequest;
import org.glue.glue_be.post.dto.response.*;
import org.glue.glue_be.post.service.PostService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Post", description = "게시글 관련 API")
public class PostController {

	private final PostService postService;


	// 1. 게시글 작성
	@PostMapping
	@Operation(summary = "게시글 작성")
	public BaseResponse<CreatePostResponse> createPost(@RequestBody @Valid CreatePostRequest req, @AuthenticationPrincipal CustomUserDetails auth) {
		return new BaseResponse<>(postService.createPost(req, auth.getUserId()));
	}


	// 2. 게시글 단건 조회
	@GetMapping("/{postId}")
	@Operation(summary = "게시글 상세 조회")
	public BaseResponse<GetPostResponse> getPost(@PathVariable Long postId, @AuthenticationPrincipal CustomUserDetails auth) {
		return new BaseResponse<>(postService.getPost(postId, auth.getUserId()));
	}

	// 3. 게시글 수정
	@PostMapping("/{postId}")
	public BaseResponse<Void> updatePost(@PathVariable Long postId, @AuthenticationPrincipal CustomUserDetails auth,
		@RequestBody @Valid UpdatePostRequest req) {
		postService.updatePost(postId, auth.getUserId(), req);
		return new BaseResponse<>();
	}

	// 4. 게시글 삭제
	@DeleteMapping("/{postId}")
	public BaseResponse<Void> deletePost(@PathVariable Long postId, @AuthenticationPrincipal CustomUserDetails auth) {
		postService.deletePost(postId, auth.getUserId());
		return new BaseResponse<>();
	}


	// 5. 게시글 끌올
	@GetMapping("/{postId}/bump")
  @Operation(summary = "게시글 끌올")
	public BaseResponse<BumpPostResponse> bumpPost(@PathVariable Long postId, @AuthenticationPrincipal CustomUserDetails auth) {
		return new BaseResponse<>(postService.bumpPost(postId, auth.getUserId()));
	}

	// 6. 게시글 목록 조회 (무한 스크롤 / cursor 기반)
	// - bumpedAt 가 있는 글이 먼저 우선적으로 내림차순으로 최근 끌올순 구현
	// - bumpedAt 가 없는 글들 중에선 createdAt 순
	@GetMapping
  @Operation(summary = "게시글 전체 조회")
	public BaseResponse<GetPostsResponse> getPosts(
		@RequestParam(required = false) Long lastPostId,
	public BaseResponse<GetPostsResponse> getPosts(@RequestParam(required = false) Long lastPostId,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(required = false) Integer categoryId,
		@RequestParam(defaultValue = "false") boolean languageToggle,   // 맞춤언어
		@AuthenticationPrincipal CustomUserDetails auth
	) {
		GetPostsResponse response = postService.getPosts(
			lastPostId, size, categoryId, languageToggle, auth.getUserId()
		);
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


	// 8. 좋아요 등록(토글)
	@GetMapping("/{postId}/like")
	@Operation(summary = "게시글 좋아요")
	public BaseResponse<Void> toggleLike(@PathVariable Long postId, @AuthenticationPrincipal CustomUserDetails auth) {
		postService.toggleLike(postId, auth.getUserId());
		return new BaseResponse<>();
	}

	// 9. 홈화면 인기 게시글
	@GetMapping("/popular")
	public BaseResponse<?> getPopularPosts(@AuthenticationPrincipal CustomUserDetails auth,
		@RequestParam(defaultValue = "2") int size) {
		if(size <= 2){
			List<MainPagePostResponse> list = postService.getMainPagePosts(size, auth.getUserId());
			return new BaseResponse<>(list);
		} else {
			GetPostsResponse response = postService.getPopularDetailed(size, auth.getUserId());
			return new BaseResponse<>(response);
		}
	}


	 // 9. 언어 매칭 게시글 조회
	@GetMapping("/language-match")
	public BaseResponse<?> getLanguageMatch(
		@RequestParam(defaultValue = "2") int size,
		@AuthenticationPrincipal CustomUserDetails auth
	) {
		Long userId = auth.getUserId();
		if (size <= 2) {
			List<MainPagePostResponse> list = postService.getLanguageMatchedMain(size, userId);
			return new BaseResponse<>(list);
		} else {
			GetPostsResponse resp = postService.getLanguageMatchedDetailed(size, userId);
			return new BaseResponse<>(resp);
		}
	}


}
