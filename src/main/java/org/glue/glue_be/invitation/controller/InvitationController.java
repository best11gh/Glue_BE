package org.glue.glue_be.invitation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Invitation", description = "초대장 API")
public class InvitationController {

    private final InvitationService invitationService;

    // 모임 초대장 생성 API
    @PostMapping("/meeting/{meetingId}")
    @Operation(summary = "모임 초대장 생성")
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
    @Operation(summary = "모임 초대장 수락")
    public BaseResponse<InvitationDto.AcceptResponse> acceptInvitation(@Valid @RequestBody InvitationDto.AcceptRequest request) {
        Long currentUserId = getCurrentUserId();
        InvitationDto.AcceptResponse response = invitationService.acceptInvitation(request.getCode(), currentUserId);
        return new BaseResponse<>(response);
    }

    // 초대장 목록 조회 API
    @GetMapping
    @Operation(summary = "모임 초대장 목록 조회")
    public BaseResponse<Page<InvitationDto.Response>> getInvitations(Pageable pageable) {
        Long currentUserId = getCurrentUserId();
        return new BaseResponse<>(invitationService.getInvitations(currentUserId, pageable));
    }

    // 초대장 상태 조회 API
    @GetMapping("/status/{code}")
    @Operation(summary = "초대장 상태 조회", description = "초대장 코드로 초대장의 상태를 조회합니다")
    public BaseResponse<InvitationDto.StatusResponse> getInvitationStatus(@PathVariable String code) {
        return new BaseResponse<>(invitationService.getInvitationStatus(code));
    }

    // 모임 참가 여부 확인 API
    @GetMapping("/check-participation/{meetingId}/{userId}")
    @Operation(summary = "모임 참가 여부 확인", description = "특정 사용자가 해당 모임에 참가하고 있는지 확인합니다")
    public BaseResponse<InvitationDto.ParticipationCheckResponse> checkMeetingParticipation(
            @PathVariable Long meetingId, 
            @PathVariable Long userId) {
        return new BaseResponse<>(invitationService.checkMeetingParticipation(meetingId, userId));
    }

    // 현재 로그인한 사용자 ID 가져오기
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("인증 정보를 찾을 수 없습니다.");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.glue.glue_be.auth.jwt.CustomUserDetails) {
            return ((org.glue.glue_be.auth.jwt.CustomUserDetails) principal).getUserId();
        }
        
        throw new IllegalStateException("잘못된 인증 정보입니다.");
    }
}