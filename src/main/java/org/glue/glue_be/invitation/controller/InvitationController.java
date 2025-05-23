package org.glue.glue_be.invitation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.glue.glue_be.common.response.BaseResponse;
import org.glue.glue_be.invitation.dto.InvitationDto;
import org.glue.glue_be.invitation.service.InvitationService;
import org.glue.glue_be.meeting.dto.MeetingDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    // 모임 초대장 생성 API
    @PostMapping("/meeting/{meetingId}")
    public BaseResponse<InvitationDto.Response> createMeetingInvitation(
            @PathVariable Long meetingId,
            @Valid @RequestBody MeetingDto.InvitationRequest request) {
        Long currentUserId = getCurrentUserId();

        // InvitationService에 필요한 데이터 구성
        InvitationDto.CreateRequest invitationRequest = InvitationDto.CreateRequest.builder()
                .meetingId(meetingId)
                .inviteeId(request.getInviteeId())
                .maxUses(1) // 기본값: 한 번만 사용 가능
                .expirationHours(6) // 기본값: 6시간 유효
                .build();

        return new BaseResponse<>(invitationService.createInvitation(invitationRequest, currentUserId));
    }

    // 초대장 수락 API
    @PostMapping("/accept")
    public BaseResponse<Void> acceptInvitation(@Valid @RequestBody InvitationDto.AcceptRequest request) {
        Long currentUserId = getCurrentUserId();
        invitationService.acceptInvitation(request.getCode(), currentUserId);
        return new BaseResponse<>();
    }

    // 초대장 목록 조회 API
    @GetMapping
    public BaseResponse<Page<InvitationDto.Response>> getInvitations(Pageable pageable) {
        Long currentUserId = getCurrentUserId();
        return new BaseResponse<>(invitationService.getInvitations(currentUserId, pageable));
    }

    // 현재 로그인한 사용자 ID 가져오기
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("인증 정보를 찾을 수 없습니다.");
        }
        return Long.parseLong(authentication.getPrincipal().toString());
    }
}