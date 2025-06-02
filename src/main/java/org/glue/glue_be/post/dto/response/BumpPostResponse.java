package org.glue.glue_be.post.dto.response;


public record BumpPostResponse(
	int bumpCount,
	int maxBumpCount
) {}
