package org.glue.glue_be.meeting.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.glue.glue_be.common.response.BaseResponse;
import org.glue.glue_be.meeting.dto.MeetingDto;
import org.glue.glue_be.meeting.service.MeetingService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings")
public class MeetingController {

    private final MeetingService meetingService;

    /**
     * 모임 생성 API
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<MeetingDto.CreateResponse> createMeeting(@Valid @RequestBody MeetingDto.CreateRequest request) {
        Long currentUserId = getCurrentUserId();
        return new BaseResponse<>(meetingService.createMeeting(request, currentUserId));
    }

    /**
     * 모임 참여 API
     */
    @GetMapping("/{meetingId}/join")
    public BaseResponse<Void> joinMeeting(@PathVariable Long meetingId) {
        Long currentUserId = getCurrentUserId();
        meetingService.joinMeeting(meetingId, currentUserId);
        return new BaseResponse<>();
    }

    /**
     * 모임 상세 조회 API
     */
    @GetMapping("/{meetingId}")
    public BaseResponse<MeetingDto.Response> getMeeting(@PathVariable Long meetingId) {
        return new BaseResponse<>(meetingService.getMeeting(meetingId));
    }

    /**
     * 현재 로그인한 사용자 ID 가져오기
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("인증 정보를 찾을 수 없습니다.");
        }
        return Long.parseLong(authentication.getPrincipal().toString());
    }
}
