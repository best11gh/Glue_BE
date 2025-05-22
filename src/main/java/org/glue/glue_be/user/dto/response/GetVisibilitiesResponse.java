package org.glue.glue_be.user.dto.response;


import org.glue.glue_be.user.entity.User;


public record GetVisibilitiesResponse(
	int currentMajorVisibility,
	int currentMeetingHistoryVisibility,
	int currentLikeListVisibility,
	int currentGuestBookVisibility
) {
	public static GetVisibilitiesResponse from(User user) {
		return new GetVisibilitiesResponse(
			user.getMajorVisibility(),
			user.getMeetingVisibility(),
			user.getLikeVisibility(),
			user.getGuestbooksVisibility()
		);
	}
}
