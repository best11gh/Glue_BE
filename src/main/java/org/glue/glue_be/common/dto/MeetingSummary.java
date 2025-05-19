package org.glue.glue_be.common.dto;

public record MeetingSummary (
        Long meetingId,
        String meetingTitle,
        String meetingImageUrl,
        Integer currentParticipants
) {}