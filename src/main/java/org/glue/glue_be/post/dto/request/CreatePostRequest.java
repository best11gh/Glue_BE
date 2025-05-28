package org.glue.glue_be.post.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;


@Getter
public class CreatePostRequest {

	private MeetingDto meeting;
	private PostDto post;

	@Getter
	public static class MeetingDto {

		@NotBlank(message = "모임 제목은 필수값입니다.")
		private String meetingTitle;

		@NotNull(message = "카테고리 id는 필수값입니다")
		private Integer categoryId;

		@NotBlank(message = "모임 장소는 필수값입니다")
		private String meetingPlaceName;

		@NotNull(message = "모임일시는 필수값입니다")
		@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
		@Future(message = "모임 시간은 현재 시간 이후여야 합니다")
		private LocalDateTime meetingTime;

		@NotNull(message = "모임 메인언어 id는 필수값입니다")
		private Integer mainLanguageId;

		@NotNull(message = "모임 교환언어 id는 필수값입니다")
		private Integer exchangeLanguageId;

		@NotNull(message = "최대 참가자수는 필수값입니다")
		private Integer maxParticipants;
	}

	@Getter
	public static class PostDto {

		@NotBlank(message = "게시글 제목은 필수값입니다.")
		private String title;

		@NotBlank(message = "게시글 내용은 필수값입니다.")
		private String content;

		private List<String> imageUrls;

	}
}