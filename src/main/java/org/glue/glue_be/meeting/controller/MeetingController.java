package org.glue.glue_be.meeting.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.glue.glue_be.auth.jwt.CustomUserDetails;
import org.glue.glue_be.common.response.BaseResponse;
import org.glue.glue_be.meeting.dto.MeetingDto;
import org.glue.glue_be.meeting.service.MeetingService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings")
@Tag(name = "Meeting", description = "모임 관련 API")
public class MeetingController {

    private final MeetingService meetingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "모임 생성")
    public BaseResponse<MeetingDto.CreateResponse> createMeeting(@Valid @RequestBody MeetingDto.CreateRequest request,
        @AuthenticationPrincipal CustomUserDetails auth) {
        return new BaseResponse<>(meetingService.createMeeting(request, auth.getUserId()));
    }

    @GetMapping("/{meetingId}/join")
    @Operation(summary = "모임 참여")
    public BaseResponse<Void> joinMeeting(@PathVariable Long meetingId,
        @AuthenticationPrincipal CustomUserDetails auth) {
        meetingService.joinMeeting(meetingId, auth.getUserId());
        return new BaseResponse<>();
    }

    @GetMapping("/{meetingId}")
    @Operation(summary = "모임 상세 조회")
    public BaseResponse<MeetingDto.Response> getMeeting(@PathVariable Long meetingId) {
        return new BaseResponse<>(meetingService.getMeeting(meetingId));
    }

}
