package org.glue.glue_be.meeting.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.glue.glue_be.common.dto.MeetingSummary;

import java.util.List;

@Getter
@Builder
public class MyMeetingsResponse {
    
    private List<MeetingSummary> hostedMeetings;
    
    private List<MeetingSummary> joinedMeetings;
}