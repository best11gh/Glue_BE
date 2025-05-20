package org.glue.glue_be.common.dto;

public record UserSummaryWithHostInfo(UserSummary user, boolean isHost) {
    public static UserSummaryWithHostInfo from(UserSummary user, boolean isHost) {
        return new UserSummaryWithHostInfo(user, isHost);
    }
}