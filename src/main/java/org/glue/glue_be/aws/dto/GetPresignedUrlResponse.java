package org.glue.glue_be.aws.dto;


import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class GetPresignedUrlResponse {

	private String presignedUrl;

	private String publicUrl;

}
