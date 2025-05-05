package org.glue.glue_be.post.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.glue.glue_be.auth.jwt.CustomUserDetails;
import org.glue.glue_be.common.response.BaseResponse;
import org.glue.glue_be.post.dto.request.CreatePostRequest;
import org.glue.glue_be.post.dto.response.CreatePostResponse;
import org.glue.glue_be.post.service.PostService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;

	// 1. 게시글 작성 (로그인 필수)
	@PostMapping
	public BaseResponse<CreatePostResponse> createPost(@RequestBody @Valid CreatePostRequest req,
		@AuthenticationPrincipal CustomUserDetails auth) {
		String uuid = auth.getUsername();
		return new BaseResponse<>(postService.createPost(req, uuid));
	}

	// 2. 게시글 단건 조회

	// 3. 게시글 수정 (로그인 필수)

	// 4. 게시글 삭세 (로그인 필수)

	// 5. 게시글 끌올 (로그인 필수)

	// 6. 게시글 목록 조회

	// 7. 카테고리별 게시글 목록 조회

	// 8. 검색 결과 게시글 목록 조회

	// 9. 좋아요 등록(토글) (로그인 필수)






}
