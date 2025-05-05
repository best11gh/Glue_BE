package org.glue.glue_be.post.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
public class CreatePostRequest {

	private MeetingDto meeting;
	private PostDto post;

	@Getter
	public static class MeetingDto {

		private String meetingTitle;

		private Integer categoryId;

		private String meetingPlaceName;

		@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
		private LocalDateTime meetingTime;

		private Double meetingPlaceLatitude;

		private Double meetingPlaceLongitude;

		private Integer languageId;

		private Integer maxParticipants;
	}

	@Getter
	public static class PostDto {

		private String title;

		private String content;

	}
}